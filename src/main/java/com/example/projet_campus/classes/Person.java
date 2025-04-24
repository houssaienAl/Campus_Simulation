package com.example.projet_campus.classes;

public class Person {
    public String name;
    public int age;
    public double resourceConsumption;

    public Person(String name, int age, double resourceConsumption) {
        this.name = name;
        this.age = age;
        this.resourceConsumption = resourceConsumption;
    }

    public String getName() { return name; }
    public double getResourceConsumption() { return resourceConsumption; }

    // ← new setter:
    public void setName(String name) {
        this.name = name;
    }
}
