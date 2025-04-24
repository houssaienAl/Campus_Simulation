package com.example.projet_campus.classes;

public class Professor extends Person {
    public String subject;
    public boolean availability;
    public Professor(String name, int age,double resourceConsumption, String sector, int satisfaction, String subject, boolean availability) {
        super(name, age, resourceConsumption);
        this.subject = subject;
        this.availability = availability;


    }
    public String getSubject() {
        return subject;
    }
    public boolean isAvailability() {
        return availability;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

}
