package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VisionHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (this.commandConditions(server, player, EnumRole.FORTUNE_TELLER, chosenPName)) {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (player.hasUsedVision()) player.send("You have already used this hability");
            else if (chosenPlayer.isPresent()) {
                boolean isChosenPlayerWolf = chosenPlayer.get().getRole().equals(EnumRole.WOLF);
                String message = isChosenPlayerWolf ? chosenPName + " is a Wolf" : chosenPName + " isn't a Wolf";
                player.addVisions(chosenPName, isChosenPlayerWolf);
                player.setUsedVision(true);
                player.send(message);
            } else {
                player.send(chosenPName + " is unavailable");
            }
        }
    }
}
