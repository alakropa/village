package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        EnumRole role = EnumRole.WOLF;

        if (this.commandConditions(server, player, role, chosenPName)) {
            Optional<Server.PlayerHandler> playerWhoDies = server.getPlayerByName(chosenPName);
            if (playerWhoDies.isPresent()) {
                EnumRole chosenPRole = playerWhoDies.get().getCharacter().getRole();
                if (chosenPRole.equals(EnumRole.WOLF))
                    player.send("You can't target a Wolf");
                else if (!playerWhoDies.get().isAlive())
                    player.send("This player is already dead");
                else {
                    player.setVote(playerWhoDies.get());
                    player.send("You voted to kill " + playerWhoDies.get().getName());
                }
            } else player.send(chosenPName + " is unavailable");
        }
    }
}
