package Homework07;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DiscountProduct extends Product {
    private int discount;
    private LocalDate discountEndDate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public DiscountProduct(String name, int price, int discount, String discountEndDate) {
        super(name, price);
        validateDiscount(discount, price);
        this.discount = discount;
        this.discountEndDate = LocalDate.parse(discountEndDate, DATE_FORMATTER);
    }

    private void validateDiscount(int discount, int price) {
        if (discount <= 0) {
            throw new IllegalArgumentException("Скидка должна быть положительной");
        }
        if (discount >= price) {
            throw new IllegalArgumentException("Скидка не может быть больше или равна цене");
        }
    }

    @Override
    public int getPrice() {
        if (LocalDate.now().isAfter(discountEndDate)) {
            return super.getPrice(); // Возвращаем полную цену после окончания скидки
        }
        return super.getPrice() - discount;
    }

    public int getOriginalPrice() {
        return super.getPrice();
    }

    public int getDiscount() {
        return discount;
    }

    public String getDiscountEndDate() {
        return discountEndDate.format(DATE_FORMATTER);
    }

    @Override
    public String toString() {
        if (LocalDate.now().isAfter(discountEndDate)) {
            return super.toString();
        }
        return super.getName() + " (" + getOriginalPrice() + " руб.) [СКИДКА " + discount +
                " руб. до " + getDiscountEndDate() + "]";
    }
}