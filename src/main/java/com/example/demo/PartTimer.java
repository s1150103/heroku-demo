package com.example.demo;

import jakarta.persistence.*;

// ★ ポリモーフィズムのポイント5:
// PartTimerはMemberを継承 → 同じ getRoleLabel() でも「アルバイト」を返す
@Entity
@DiscriminatorValue("PART_TIMER")
public class PartTimer extends Member {

    private Integer hoursPerWeek; // 週の勤務時間

    public PartTimer() {}

    public PartTimer(String name, String department, Integer hoursPerWeek) {
        super(name, department);
        this.hoursPerWeek = hoursPerWeek;
    }

    public Integer getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Integer hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    @Override
    public String getRoleLabel() {
        return "アルバイト";
    }

    @Override
    public String[] getPermissions() {
        return new String[]{"閲覧"};
    }
}
