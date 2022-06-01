package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (this.commandConditions(server, player, EnumRole.WOLF, chosenPName)) {
            Optional<Server.PlayerHandler> playerWhoDies = server.getPlayerByName(chosenPName);
            if (playerWhoDies.isPresent()) {
                EnumRole chosenPRole = playerWhoDies.get().getRole();
                if (chosenPRole.equals(EnumRole.WOLF)) player.send("You can't target a Wolf");
                else if (playerWhoDies.get().isAlive()) player.setVote(playerWhoDies.get());
            } else {
                player.send(chosenPName + " is unavailable");
            }
        }
    }
}
