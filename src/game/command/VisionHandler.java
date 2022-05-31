package game.command;

import game.EnumRole;
import game.Server.Server;

import java.util.Optional;

public class VisionHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (player.getRole() != EnumRole.FORTUNE_TELLER) {
            player.send("You must be a Fortune Teller to use this command");
            return;
        }
        String chosenPName = player.getMessage().split(" ")[1];
        Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);

        if (chosenPlayer.isPresent()) {
            String message;
            message = chosenPlayer.get().getRole() == EnumRole.WOLF ? chosenPName + " is a Wolf" : chosenPName + " is a Villager";
            player.send(message);
        } else {
            player.send(chosenPName + " is unavailble");
        }
    }
}
