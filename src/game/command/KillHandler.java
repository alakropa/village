package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (commandConditions(server, player, EnumRole.WOLF, chosenPName)) {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (chosenPlayer.isPresent()) chosenPlayer.get().killPlayer();
            else player.send(chosenPName + " is unavailable");
        }
    }
}
