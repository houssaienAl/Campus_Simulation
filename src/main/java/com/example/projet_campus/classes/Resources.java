package com.example.projet_campus.classes;

public class Resources {
    protected double wifiUsage;        // in GB
    protected double electricityUsage; // in kWh
    protected double waterUsage;       // in Liters
    protected double spaceUsage;       // in square meters

    // Constructor
    public Resources(double wifiUsage, double electricityUsage, double waterUsage, double spaceUsage) {
        this.wifiUsage = wifiUsage;
        this.electricityUsage = electricityUsage;
        this.waterUsage = waterUsage;
        this.spaceUsage = spaceUsage;
    }

    // Getters
    public double getWifiUsage() {
        return wifiUsage;
    }

    public double getElectricityUsage() {
        return electricityUsage;
    }

    public double getWaterUsage() {
        return waterUsage;
    }

    public double getSpaceUsage() {
        return spaceUsage;
    }

    // Setters
    public void setWifiUsage(double wifiUsage) {
        this.wifiUsage = wifiUsage;
    }

    public void setElectricityUsage(double electricityUsage) {
        this.electricityUsage = electricityUsage;
    }

    public void setWaterUsage(double waterUsage) {
        this.waterUsage = waterUsage;
    }

    public void setSpaceUsage(double spaceUsage) {
        this.spaceUsage = spaceUsage;
    }

    // Method to calculate total "consumption score"
    public double calculateConsumption() {
        return wifiUsage * 0.5 + electricityUsage * 0.3 + waterUsage * 0.1 + spaceUsage * 0.1;
    }

    // Suggest optimizations
    public void optimiseResources() {
        System.out.println("Optimisation tips:");
        if (wifiUsage > 100) {
            System.out.println("- Reduce streaming or large downloads.");
        }
        if (electricityUsage > 200) {
            System.out.println("- Turn off unused devices and lights.");
        }
        if (waterUsage > 300) {
            System.out.println("- Fix leaks and encourage short showers.");
        }
        if (spaceUsage > 1000) {
            System.out.println("- Optimize room scheduling.");
        }
    }
}
