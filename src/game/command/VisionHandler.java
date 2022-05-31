package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VisionHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (chosenPName == null) player.send("Unvailble command");
        else if (player.getRole() != EnumRole.FORTUNE_TELLER && server.isNight())
            player.send("You must be a Fortune Teller to use this command");
        else {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (chosenPlayer.isPresent()) {
                String message;
                message = chosenPlayer.get().getRole().equals(EnumRole.WOLF) ? chosenPName + " is a Wolf" : chosenPName + " isn't a Wolf";
                player.send(message);
            } else {
                player.send(chosenPName + " is unavailble");
            }
        }
    }
}
