package game;

public class Helpers {
    public static boolean compareIfNamesMatch(String name1, String name2) {
        return Helpers.removeSpaces(name1).equalsIgnoreCase(name2);
    }

    private static String removeSpaces(String word) {
        return String.join("", (word.split(" ")));
    }
}
