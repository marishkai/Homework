package Homework08_1;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class UniqueElements {

    public static <T> Set<T> getUniqueElements(ArrayList<T> list) {
        // сохраняем порядок добавления элементов
        return new LinkedHashSet<>(list);
    }
}