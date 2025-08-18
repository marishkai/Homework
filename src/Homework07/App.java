import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        List<Person> people = inputPeople();
        List<Product> products = inputProducts();
        processPurchases(people, products);
        printResults(people);
    }

    private static List<Person> inputPeople() {
        List<Person> people = new ArrayList<>();
        System.out.println("=== Ввод покупателей ===");
        System.out.println("Введите покупателей в формате: Имя = Сумма (разделяйте точкой с запятой)");
        System.out.println("Пример: Иван = 5000; Анна = 3000");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("Ввод не может быть пустым. Повторите попытку.");
                continue;
            }

            boolean hasErrors = false;
            String[] peopleInput = input.split("\\s*;\\s*");
            List<String> errorMessages = new ArrayList<>();

            List<Person> validPeople = new ArrayList<>();

            for (String personData : peopleInput) {
                try {
                    String[] parts = personData.split("\\s*=\\s*");
                    if (parts.length != 2) {
                        errorMessages.add("Ошибка в '" + personData + "': Неверный формат. Используйте: Имя = Сумма");
                        hasErrors = true;
                        continue;
                    }

                    String name = parts[0].trim();
                    if (name.isEmpty()) {
                        errorMessages.add("Ошибка в '" + personData + "': Имя не может быть пустым");
                        hasErrors = true;
                        continue;
                    }

                    try {
                        int money = Integer.parseInt(parts[1].trim());
                        if (money < 0) {
                            errorMessages.add("Ошибка в '" + personData + "': Сумма не может быть отрицательной");
                            hasErrors = true;
                            continue;
                        }

                        new Person(name, money);
                        validPeople.add(new Person(name, money));
                    } catch (NumberFormatException e) {
                        errorMessages.add("Ошибка в '" + personData + "': Некорректная сумма. Введите целое число");
                        hasErrors = true;
                    }
                } catch (IllegalArgumentException e) {
                    errorMessages.add("Ошибка в '" + personData + "': " + e.getMessage());
                    hasErrors = true;
                }
            }

            if (hasErrors) {
                System.out.println("Обнаружены ошибки:");
                errorMessages.forEach(System.out::println);
                System.out.println("Повторите ввод всех покупателей:");
                continue;
            }

            if (!validPeople.isEmpty()) {
                people.addAll(validPeople);
                break;
            } else {
                System.out.println("Не введено ни одного корректного покупателя. Повторите попытку.");
            }
        }
        return people;
    }

    private static List<Product> inputProducts() {
        List<Product> products = new ArrayList<>();
        System.out.println("\n=== Ввод продуктов ===");
        System.out.println("Вводите продукты по одному. Для завершения введите END");

        int productCount = 0;
        while (true) {
            // Ввод названия продукта с проверкой
            String name;
            while (true) {
                System.out.print("\nНазвание продукта (или END): ");
                name = scanner.nextLine().trim();

                if (name.equalsIgnoreCase("END")) {
                    if (productCount == 0) {
                        System.out.println("Должен быть добавлен хотя бы один продукт!");
                        continue;
                    }
                    return products;
                }

                if (name.isEmpty()) {
                    System.out.println("Ошибка: Название продукта не может быть пустым!");
                    continue;
                }

                if (name.length() < 3) {
                    System.out.println("Ошибка: Название должно содержать минимум 3 символа!");
                    continue;
                }

                if (name.matches("^\\d+$")) {
                    System.out.println("Ошибка: Название не может состоять только из цифр!");
                    continue;
                }

                break;
            }

            // Ввод и проверка цены
            int price = 0;
            while (true) {
                try {
                    System.out.print("Цена: ");
                    price = Integer.parseInt(scanner.nextLine().trim());
                    if (price <= 0) {
                        System.out.println("Ошибка: Цена должна быть положительной. Введите цену снова:");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: Введите корректное число!");
                }
            }

            // Ввод информации о скидке
            System.out.print("Это скидочный продукт? (да/нет): ");
            String isDiscount = scanner.nextLine().trim().toLowerCase();

            if (isDiscount.equals("да")) {
                // Ввод и проверка скидки
                int discount = 0;
                while (true) {
                    try {
                        System.out.print("Размер скидки: ");
                        discount = Integer.parseInt(scanner.nextLine().trim());
                        if (discount <= 0) {
                            System.out.println("Ошибка: Скидка должна быть положительной. Введите скидку снова:");
                            continue;
                        }
                        if (discount >= price) {
                            System.out.println("Ошибка: Скидка не может быть больше или равна цене. Введите скидку снова:");
                            continue;
                        }
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка: Введите корректное число!");
                    }
                }

                // Ввод даты окончания скидки
                LocalDate endDate = null;
                while (true) {
                    try {
                        System.out.print("Скидка действует до (дд-мм-гггг): ");
                        String dateStr = scanner.nextLine().trim();
                        endDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                        break;
                    } catch (Exception e) {
                        System.out.println("Ошибка: Неверный формат даты. Используйте дд-мм-гггг");
                    }
                }

                products.add(new DiscountProduct(name, price, discount, endDate.format(DATE_FORMATTER)));
            } else {
                products.add(new Product(name, price));
            }
            productCount++;
        }
    }

    private static void processPurchases(List<Person> people, List<Product> products) {
        System.out.println("\n=== Обработка покупок ===");
        System.out.println("Вводите покупки в формате: Имя покупателя - Название продукта");
        System.out.println("Для завершения введите END");

        while (true) {
            System.out.print("\n> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("END")) {
                if (!people.isEmpty()) break;
                System.out.println("Нужно сделать хотя бы одну покупку!");
                continue;
            }

            try {
                String[] parts = input.split("\\s*-\\s*");
                if (parts.length != 2) {
                    throw new IllegalArgumentException("Неверный формат. Используйте: Имя - Продукт");
                }

                String personName = parts[0].trim();
                String productName = parts[1].trim();

                Person person = findPerson(people, personName);
                Product product = findProduct(products, productName);

                if (person == null) throw new IllegalArgumentException("Покупатель '" + personName + "' не найден");
                if (product == null) throw new IllegalArgumentException("Продукт '" + productName + "' не найден");

                person.buyProduct(product);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private static void printResults(List<Person> people) {
        System.out.println("\n=== Итоги покупок ===");
        people.forEach(System.out::println);
    }

    private static Person findPerson(List<Person> people, String name) {
        return people.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static Product findProduct(List<Product> products, String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}