package Homework08_1;
import java.util.ArrayList;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> words = new ArrayList<>();
        words.add("apple");
        words.add("banana");
        words.add("apple");
        words.add("orange");
        words.add("banana");

        Set<String> uniqueWords = UniqueElements.getUniqueElements(words);
        System.out.println("Исходный список: " + words);
        System.out.println("Уникальные элементы (с сохранением порядка): " + uniqueWords);

        // Дополнительный пример с числами
        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(20);
        numbers.add(10);
        numbers.add(30);
        numbers.add(20);

        Set<Integer> uniqueNumbers = UniqueElements.getUniqueElements(numbers);
        System.out.println("\nИсходный список чисел: " + numbers);
        System.out.println("Уникальные числа: " + uniqueNumbers);
    }
}