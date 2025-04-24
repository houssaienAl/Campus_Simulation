package com.example.projet_campus.classes;

public class Student extends Person {
    private int satisfaction;   // renamed for Java style
    private String sector;

    public Student(String name, int age, double resourceConsumption, String sector, int satisfaction) {
        super(name, age, resourceConsumption);
        this.sector = sector;
        this.satisfaction = satisfaction;
    }

    public String getSector() {
        return sector;
    }
    public int getSatisfaction() {
        return satisfaction;
    }

    // ← new setters:
    public void setSector(String sector) {
        this.sector = sector;
    }
    public void setSatisfaction(int satisfaction) {
        this.satisfaction = satisfaction;
    }
}
