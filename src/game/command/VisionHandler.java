package game.command;

import game.Characters.FortuneTeller;
import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VisionHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        EnumRole role = EnumRole.FORTUNE_TELLER;

        if (this.commandConditions(server, player, role, chosenPName)) {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            FortuneTeller playerCharacter = ((FortuneTeller) player.getCharacter());

            if (playerCharacter.hasUsedVision()) player.send("You have already used this ability");
            else if (chosenPlayer.isPresent()) {
                EnumRole chosenPRole = chosenPlayer.get().getCharacter().getRole();
                boolean isChosenPlayerWolf = chosenPRole.equals(EnumRole.WOLF);
                String message = isChosenPlayerWolf ? chosenPName + " is a Wolf" : chosenPName + " isn't a Wolf";
                playerCharacter.addVisions(chosenPName, isChosenPlayerWolf);
                playerCharacter.setUsedVision(true);
                player.send(message);
            } else {
                player.send(chosenPName + " is unavailable");
            }
        }
    }
}
