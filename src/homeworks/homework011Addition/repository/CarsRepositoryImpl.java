package homeworks.homework011Addition.repository;

import homeworks.homework011Addition.model.Car;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class CarsRepositoryImpl implements CarsRepository {
    private static final String FILE_NAME = "data/cars.txt";
    private List<Car> cars;

    public CarsRepositoryImpl() {
        this.cars = new ArrayList<>();
        ensureDataDirectoryExists();
        loadCarsFromFile();
    }

    private void ensureDataDirectoryExists() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            if (dataDir.mkdir()) {
                System.out.println("Создана папка data");
            }
        }
    }

    // Все остальные методы остаются без изменений, только импорты
    @Override
    public List<Car> getAllCars() {
        return new ArrayList<>(cars);
    }

    @Override
    public void saveCars(List<Car> cars) {
        this.cars = new ArrayList<>(cars);
        saveCarsToFile();
    }

    @Override
    public List<String> findLicensePlatesByColorOrMileage(String colorToFind, int mileageToFind) {
        return cars.stream()
                .filter(car -> car.getColor().equalsIgnoreCase(colorToFind) || car.getMileage() == mileageToFind)
                .map(Car::getLicensePlate)
                .collect(Collectors.toList());
    }

    @Override
    public long countUniqueModelsInPriceRange(double minPrice, double maxPrice) {
        return cars.stream()
                .filter(car -> car.getPrice() >= minPrice && car.getPrice() <= maxPrice)
                .map(Car::getModel)
                .distinct()
                .count();
    }

    @Override
    public Optional<String> findColorOfCheapestCar() {
        return cars.stream()
                .min(Comparator.comparingDouble(Car::getPrice))
                .map(Car::getColor);
    }

    @Override
    public double getAveragePriceByModel(String modelToFind) {
        return cars.stream()
                .filter(car -> car.getModel().equalsIgnoreCase(modelToFind))
                .mapToDouble(Car::getPrice)
                .average()
                .orElse(0.0);
    }

    private void loadCarsFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            createSampleData();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    Car car = new Car(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            Integer.parseInt(parts[3].trim()),
                            Double.parseDouble(parts[4].trim())
                    );
                    cars.add(car);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    private void createSampleData() {
        cars = Arrays.asList(
                new Car("a123me", "Mercedes", "White", 0, 8300000),
                new Car("b873of", "Volga", "Black", 0, 673000),
                new Car("w487mn", "Lexus", "Grey", 76000, 900000),
                new Car("p987hj", "Volga", "Red", 610, 704340),
                new Car("c987ss", "Toyota", "White", 254000, 761000),
                new Car("o983op", "Toyota", "Black", 698000, 740000),
                new Car("p146op", "BMW", "White", 271000, 850000),
                new Car("u893ii", "Toyota", "Purple", 210900, 440000),
                new Car("l097df", "Toyota", "Black", 108000, 780000),
                new Car("y876wd", "Toyota", "Black", 160000, 1000000)
        );
        saveCarsToFile();
    }

    private void saveCarsToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Car car : cars) {
                writer.write(car.getLicensePlate() + "|" +
                        car.getModel() + "|" +
                        car.getColor() + "|" +
                        car.getMileage() + "|" +
                        (int)car.getPrice());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при записи файла: " + e.getMessage());
        }
    }
}