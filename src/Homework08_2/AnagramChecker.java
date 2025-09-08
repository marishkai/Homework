package Homework08_2;

import java.util.Arrays;
import java.util.Scanner;

public class AnagramChecker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Введите первую строку:");
        String s = scanner.nextLine().toLowerCase().replaceAll("\\s+", "");

        System.out.println("Введите вторую строку:");
        String t = scanner.nextLine().toLowerCase().replaceAll("\\s+", "");

        if (s.length() != t.length()) {
            System.out.println("false");
            return;
        }

        char[] sArray = s.toCharArray();
        char[] tArray = t.toCharArray();

        Arrays.sort(sArray);
        Arrays.sort(tArray);

        boolean result = Arrays.equals(sArray, tArray);
        System.out.println("Результат: " + result);

        scanner.close();
    }
}