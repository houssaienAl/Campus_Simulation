package com.example.projet_campus.classes;

public class Building {
    public int id;
    public String name;
    public String type;
    public int capacity;
    public double resourceConsumption;
    public int satisfactionImpact;

    public Building(int id, String name, String type, int capacity, double resourceConsumption, int satisfactionImpact) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
        this.resourceConsumption = resourceConsumption;
        this.satisfactionImpact = satisfactionImpact;
    }
    public String getType() {
        return type;
    }
    public void displayInfo() {
        System.out.println("🔹 Building [" + type + "]: " + name);
        System.out.println("   - ID: " + id);
        System.out.println("   - Capacity: " + capacity);
        System.out.println("   - Resource Consumption: " + resourceConsumption);
        System.out.println("   - Satisfaction Impact: " + satisfactionImpact);
    }

    public int getSatisfactionImpact() {
        return satisfactionImpact;
    }

    public double getResourceConsumption() {
        return resourceConsumption;
    }

    public String getName() {
        return name;
    }

    public void add(Building building) {
        building.add(building);
        System.out.println("Building added: " + building.getName());

    }

    public int getCapacity() {
        return capacity;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
}
