package game;

public class Helpers {
    public static boolean compareIfNamesMatch(String name1, String name2) {
        return Helpers.removeSpaces(name1).equalsIgnoreCase(name2);
    }

    private static String removeSpaces(String word) {
        return String.join("", (word.split(" ")));
    }

    public static String removeCommand(String message) {
        String[] words = message.split(" ", 2);
        if (words.length >= 2) return words[1];
        return null;
    }
}
