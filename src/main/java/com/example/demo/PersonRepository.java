package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepositoryを継承するだけでSELECT/INSERT/DELETE等が使える
public interface PersonRepository extends JpaRepository<Person, Long> {
}
