package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;
import game.colors.Colors;
import game.colors.ColorsRef;

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
                    player.send(Colors.RED + "You can't target a Wolf" + ColorsRef.RESET.getCode());
                else if (!playerWhoDies.get().isAlive())
                    player.send(Colors.RED + "This player is already dead" + ColorsRef.RESET.getCode());
                else {
                    player.setVote(playerWhoDies.get());
                    player.send(Colors.RED + "You voted to kill " + playerWhoDies.get().getName() + ColorsRef.RESET.getCode());
                }
            } else player.send(Colors.RED + chosenPName + " is unavailable" + ColorsRef.RESET.getCode());
        }
    }
}
