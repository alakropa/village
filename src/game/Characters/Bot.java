package game.Characters;

import game.EnumRole;
import game.Server.Server;

import java.util.List;
import java.util.Optional;

public class Bot extends Character {
    private final String NAME;
    private static int botNumber;

    public Bot() {
        this.NAME = "Bot" + ++botNumber;
    }

    public String getNAME() {
        return NAME;
    }

    public Optional<Server.PlayerHandler> getDayVote(Server server) {
        List<Server.PlayerHandler> playersList = server.getPLAYERS().values().stream().toList();

        return playersList.stream()
                .filter(x -> !x.getName().equals(this.NAME))
                .findAny();
    }

    public Optional<Server.PlayerHandler> getNightVote(Server server) {
        List<Server.PlayerHandler> playersList = server.getPLAYERS().values().stream().toList();
        return playersList.stream()
                .filter(x -> x.isAlive() &&
                        !x.getName().equals(this.NAME) &&
                        !x.getCharacter().getRole().equals(EnumRole.WOLF))
                .findAny();
    }

    @Override
    public String toString() {
        return "Bot{" +
                "NAME='" + NAME + '\'' + super.getRole() +
                '}';
    }

    public static void resetBotNumber() {
        botNumber = 0;
    }
}
