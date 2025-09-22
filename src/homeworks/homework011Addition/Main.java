package homeworks.homework011Addition;

import homeworks.homework011Addition.model.Car;
import homeworks.homework011Addition.repository.CarsRepository;
import homeworks.homework011Addition.repository.CarsRepositoryImpl;
import java.util.List;
import java.util.Optional;


public class Main {
    public static void main(String[] args) {
        CarsRepository repository = new CarsRepositoryImpl();

        System.out.println("Автомобили в базе:");
        System.out.println("Number Model Color Mileage Cost");
        for (Car car : repository.getAllCars()) {
            System.out.println(car);
        }
        System.out.println();

        // Остальной код Main без изменений
        String colorToFind = "Black";
        int mileageToFind = 0;

        List<String> licensePlates = repository.findLicensePlatesByColorOrMileage(colorToFind, mileageToFind);
        System.out.print("Номера автомобилей по цвету или пробегу: ");
        for (int i = 0; i < licensePlates.size(); i++) {
            if (i > 0 && i % 4 == 0) System.out.println();
            System.out.print(licensePlates.get(i) + " ");
        }
        System.out.println("\n");

        double minPrice = 700000;
        double maxPrice = 800000;
        long uniqueModelsCount = repository.countUniqueModelsInPriceRange(minPrice, maxPrice);
        System.out.println("Уникальные автомобили: " + uniqueModelsCount + " шт.\n");

        Optional<String> cheapestColor = repository.findColorOfCheapestCar();
        System.out.println("Цвет автомобиля с минимальной стоимостью: " + cheapestColor.orElse("не найден") + "\n");

        String[] modelsToFind = {"Toyota", "Volvo"};
        for (String model : modelsToFind) {
            double averagePrice = repository.getAveragePriceByModel(model);
            System.out.printf("Средняя стоимость модели %s: %.2f%n", model, averagePrice);
        }
    }
}