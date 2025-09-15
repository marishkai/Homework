package homeworks.homework011Addition.model;

public class Car {
    private String licensePlate;
    private String model;
    private String color;
    private int mileage;
    private double price;

    public Car(String licensePlate, String model, String color, int mileage, double price) {
        this.licensePlate = licensePlate;
        this.model = model;
        this.color = color;
        this.mileage = mileage;
        this.price = price;
    }

    // Геттеры и сеттеры
    public String getLicensePlate() {
        return licensePlate;
    }
    public String getModel() {
        return model;
    }
    public String getColor() {
        return color;
    }
    public int getMileage() {
        return mileage;
    }
    public double getPrice() {
        return price;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return licensePlate + " " + model + " " + color + " " + mileage + " " + (int)price;
    }
};