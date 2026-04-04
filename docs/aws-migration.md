# Heroku → AWS 移行設計メモ

Spring Boot + Heroku Postgres を AWS に移行する際の構成提案・Terraform メモ。

---

## アーキテクチャ構成

```
Internet
    ↓
[Route 53] - ドメイン管理
    ↓
[ALB] - ロードバランサー（パブリックサブネット）
    ↓
[ECS Fargate] - Spring Boot（プライベートサブネット）
    ↓
[Aurora Serverless v2] - PostgreSQL（プライベートサブネット）
    ↓
[Secrets Manager] - DB接続情報
```

---

## Heroku vs AWS 対応表

| Heroku | AWS |
|--------|-----|
| Dyno | ECS Fargate |
| Heroku Postgres | Aurora Serverless v2 |
| Config Vars | Secrets Manager |
| Heroku Domains | Route 53 + ALB |
| Procfile | ECS Task Definition |

---

## コスト試算（開発環境・東京リージョン）

| サービス | 月額 |
|---------|------|
| Fargate（0.5vCPU/1GB） | ~$18 |
| Aurora Serverless v2 | ~$45-90 |
| ALB | ~$16 |
| **合計** | **~$80-120/月** |

---

## ネットワーク構成

| サブネット | 配置するサービス |
|-----------|----------------|
| パブリック | ALB |
| プライベート | ECS Fargate、Aurora |

- VPC CIDR: `10.0.0.0/16`
- AZ: `ap-northeast-1a`、`ap-northeast-1c`
- NAT Gateway経由でFargateからインターネットアクセス

---

## セキュリティ構成

| Security Group | 許可するトラフィック |
|---------------|-------------------|
| ALB SG | 0.0.0.0/0 → 80番ポート |
| App SG | ALB SG → 8080番ポート |
| DB SG | App SG → 5432番ポート |

---

## Terraform

```hcl
# versions.tf
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-1"
}

# VPC
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "peacemind-vpc"
  cidr = "10.0.0.0/16"

  azs              = ["ap-northeast-1a", "ap-northeast-1c"]
  public_subnets   = ["10.0.1.0/24", "10.0.2.0/24"]
  private_subnets  = ["10.0.11.0/24", "10.0.12.0/24"]

  enable_nat_gateway = true
}

# ECR
resource "aws_ecr_repository" "app" {
  name = "peacemind-app"
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "peacemind-cluster"
}

# ECS Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = "peacemind-app"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024

  container_definitions = jsonencode([{
    name  = "app"
    image = "${aws_ecr_repository.app.repository_url}:latest"
    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]
    environment = [{
      name  = "SPRING_PROFILES_ACTIVE"
      value = "prod"
    }]
    secrets = [{
      name      = "DATABASE_URL"
      valueFrom = aws_secretsmanager_secret.db.arn
    }]
  }])
}

# ALB
resource "aws_lb" "main" {
  name               = "peacemind-alb"
  internal           = false
  load_balancer_type = "application"
  subnets            = module.vpc.public_subnets
}

resource "aws_lb_target_group" "app" {
  name        = "peacemind-tg"
  port        = 8080
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = module.vpc.vpc_id

  health_check {
    path = "/api/status"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.main.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.app.arn
  }
}

# ECS Service
resource "aws_ecs_service" "app" {
  name            = "peacemind-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = module.vpc.private_subnets
    security_groups = [aws_security_group.app.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = 8080
  }
}

# Aurora Serverless v2
resource "aws_rds_cluster" "db" {
  cluster_identifier          = "peacemind-db"
  engine                      = "aurora-postgresql"
  engine_mode                 = "provisioned"
  engine_version              = "15.4"
  database_name               = "peacemind"
  master_username             = "admin"
  manage_master_user_password = true

  serverlessv2_scaling_configuration {
    min_capacity = 0.5
    max_capacity = 2.0
  }

  vpc_security_group_ids = [aws_security_group.db.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name
}

resource "aws_db_subnet_group" "main" {
  name       = "peacemind-db-subnet"
  subnet_ids = module.vpc.private_subnets
}

# Secrets Manager
resource "aws_secretsmanager_secret" "db" {
  name = "peacemind/db-url"
}

# Security Groups
resource "aws_security_group" "alb" {
  name   = "peacemind-alb-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "app" {
  name   = "peacemind-app-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_security_group" "db" {
  name   = "peacemind-db-sg"
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.app.id]
  }
}

# Output
output "alb_dns" {
  value = aws_lb.main.dns_name
}
```

---

## デプロイ手順

```bash
# 1. Dockerイメージをビルド
./mvnw clean package -DskipTests
docker build -t peacemind-app .

# 2. ECRにプッシュ
aws ecr get-login-password | docker login --username AWS --password-stdin <ECR_URL>
docker push <ECR_URL>/peacemind-app:latest

# 3. Terraformでインフラ構築
terraform init
terraform plan
terraform apply
```

---

## 参考

- [awslabs/agent-plugins](https://github.com/awslabs/agent-plugins) の `deploy-on-aws` プラグインを参考に設計
- draw.io図: `docs/aws-migration.drawio`
