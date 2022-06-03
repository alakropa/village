package game;

import game.Server.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class Helpers {
    public static boolean compareIfNamesMatch(String name1, String name2) {
        return Helpers.removeSpaces(name1).equalsIgnoreCase(name2);
    }

    private static String removeSpaces(String word) {
        return String.join("", (word.split(" "))).trim();
    }

    public static String removeCommand(String message) {
        String[] words = message.split(" ", 2);
        if (words.length >= 2) return words[1];
        return null;
    }

    public static ArrayList<EnumRole> generateEnumCards(HashMap<String, Server.PlayerHandler> players, int playersInGame) {
        ArrayList<EnumRole> roles = new ArrayList<>(players.size());
        for (int i = 0; i < playersInGame; i++) {
            switch (i) {
                case 0, 11 -> roles.add(i, EnumRole.WOLF);
                case 1, 9 -> roles.add(i, EnumRole.FORTUNE_TELLER);
                case 6 -> roles.add(i, EnumRole.GUARD);
                default -> roles.add(i, EnumRole.VILLAGER);
            }
        }
        return roles;
    }
}
