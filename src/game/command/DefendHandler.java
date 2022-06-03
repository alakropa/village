package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class DefendHandler implements CommandHandler {

    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String defendedName = Helpers.removeCommand(player.getMessage());
        EnumRole role = EnumRole.GUARD;

        if (this.commandConditions(server, player, role, defendedName)) {
            Optional<Server.PlayerHandler> defendedPlayer = server.getPlayerByName(defendedName);
            if (defendedPlayer.isPresent()) {
                if (player.getPreviousDefend() != null) {
                    player.getPreviousDefend().setDefended(false);
                }
                player.setPreviousDefend(defendedPlayer.get());
                player.setDefended(true);
                player.send("You defend " + defendedPlayer.get().getName());
            } else {
                player.send("Player is unavailable.");
            }
        }
    }
}
