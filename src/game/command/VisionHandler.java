package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.HashMap;
import java.util.Optional;

public class VisionHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (!server.isGameInProgress()) player.send("Game hasn't started yet");
        else if (!player.getRole().equals(EnumRole.FORTUNE_TELLER)){
            player.send("You must be a Fortune Teller to use this command");
        } else if (!server.isNight()){
            player.send("You can only use this ability at night");
        } else if (chosenPName == null) {
            player.send("You need to write a name");
        } else {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (chosenPlayer.isPresent()) {
                String message;
                message = chosenPlayer.get().getRole().equals(EnumRole.WOLF) ? chosenPName + " is a Wolf" : chosenPName + " isn't a Wolf";
                player.send(message);
            } else {
                player.send(chosenPName + " is unavailable");
            }
        }
    }
}
