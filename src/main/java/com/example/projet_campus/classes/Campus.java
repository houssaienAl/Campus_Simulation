package com.example.projet_campus.classes;

import java.util.*;
public class Campus {
    private List<Building> buildings;
    private List<Person> people;
    private Resources resources;

    public Campus() {
        this.buildings = new ArrayList<>();
        this.people = new ArrayList<>();
        this.resources = new Resources(0, 0, 0, 0); // initialize properly
    }

    public void addBuilding(Building b) {
        buildings.add(b);
        resources = new Resources(
                resources.getWifiUsage() ,
                resources.getElectricityUsage() ,
                resources.getWaterUsage()  ,
                resources.getSpaceUsage() + b.getResourceConsumption()
        );
    }
    public void clear() {
        buildings.clear();
        people.clear();
        resources = new Resources(0,0,0,0);
    }
    public List<Building> getBuildings() {
        return buildings;
    }

    public void addPerson(Person p) {
        people.add(p);
        resources = new Resources(
                resources.getWifiUsage() + p.getResourceConsumption(),
                resources.getElectricityUsage() +p.getResourceConsumption() ,
                resources.getWaterUsage() + p.getResourceConsumption() ,
                resources.getSpaceUsage() + p.getResourceConsumption()
        );
    }

    public List<Person> getPeople() { return people; }

    public double getAverageSatisfaction() {
        int total = 0;
        int count = 0;
        for (Person p : people) {
            if (p instanceof Student s) {
                total += s.getSatisfaction();
                count++;
            }
        }
        return count == 0 ? 0 : (double) total / count;
    }

    public Resources getResources() { return resources; }
}