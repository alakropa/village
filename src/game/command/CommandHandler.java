package game.command;

import game.EnumRole;
import game.Server.Server;

public interface CommandHandler {
    void command(Server server, Server.PlayerHandler player);

    default boolean commandConditions(Server server, Server.PlayerHandler player, EnumRole role, String chosenPName) {
        if (!server.isGameInProgress()) {
            player.send("You must start the game first");
            return false;
        } else if (!player.isAlive()) {
            player.send("You are dead");
            return false;
        } else if (!player.getCharacter().getRole().equals(role)) {
            player.send("Only " + role.getPLURAL() + " can use this command");
            return false;
        } else if (!server.isNight()) {
            player.send("You can't use this command during day time");
            return false;
        } else if (chosenPName == null || chosenPName.equals("")) {
            player.send("You need to write a name");
            return false;
        } else if (chosenPName.equalsIgnoreCase(player.getName())) {
            player.send("You can't target yourself");
            return false;
        }
        return true;
    }

    default boolean commandConditions(Server server, Server.PlayerHandler player, String votedPName) {
        if (!server.isGameInProgress()) {
            player.send("You must start the game first");
            return false;
        } else if (server.getNumOfDays() == 0) {
            player.send("You can't vote on the 1st day!");
            return false;
        } else if (!player.isAlive()) {
            player.send("You are dead");
            return false;
        } else if (server.isNight()) {
            player.send("You can't use this command at night");
            return false;
        } else if (votedPName == null || votedPName.equals("")) {
            player.send("You need to write a name");
            return false;
        } else if (votedPName.equals(player.getName())) {
            player.send("You can't vote on yourself!");
            return false;
        }
        return true;
    }
}
