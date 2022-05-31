package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (player.getRole() != EnumRole.WOLF) {
            player.send("Only wolves can use this command");
            return;
        }

        String chosenPName = Helpers.removeCommand(player.getMessage());
        Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);

        if (chosenPlayer.isPresent()) chosenPlayer.get().killPlayer();
        else player.send(chosenPName + " is unavailable");
    }
}
