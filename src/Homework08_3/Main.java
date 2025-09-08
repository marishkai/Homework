package Homework08_3;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        PowerfulSet powerfulSet = new PowerfulSet();

        // Создаем множества
        Set<Integer> set1 = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> set2 = new HashSet<>(Arrays.asList(0, 1, 2, 4));

        System.out.println("set1 = " + formatSet(set1));
        System.out.println("set2 = " + formatSet(set2));
        System.out.println();

        // Тестируем все методы
        Set<Integer> intersectionResult = powerfulSet.intersection(set1, set2);
        System.out.println("intersection(set1, set2) = " + formatSet(intersectionResult));

        Set<Integer> unionResult = powerfulSet.union(set1, set2);
        System.out.println("union(set1, set2) = " + formatSet(unionResult));

        Set<Integer> complementResult = powerfulSet.relativeComplement(set1, set2);
        System.out.println("relativeComplement(set1, set2) = " + formatSet(complementResult));
    }

    // Вспомогательный метод для красивого вывода с фигурными скобками
    private static <T> String formatSet(Set<T> set) {
        return set.toString().replace('[', '{').replace(']', '}');
    }
}