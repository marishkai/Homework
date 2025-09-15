package homeworks.homework011Addition.repository;

import homeworks.homework011Addition.model.Car;
import java.util.List;
import java.util.Optional;

public interface CarsRepository {
    List<Car> getAllCars();
    void saveCars(List<Car> cars);
    List<String> findLicensePlatesByColorOrMileage(String colorToFind, int mileageToFind);
    long countUniqueModelsInPriceRange(double minPrice, double maxPrice);
    Optional<String> findColorOfCheapestCar();
    double getAveragePriceByModel(String modelToFind);
}