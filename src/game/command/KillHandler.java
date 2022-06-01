package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String chosenPName = Helpers.removeCommand(player.getMessage());
        if (!server.isGameInProgress()) player.send("Game hasn't started yet");
        else if (chosenPName == null) player.send("Unavailable command");
        else if (!player.getRole().equals(EnumRole.WOLF)) player.send("Only wolves can use this command");
        else if (!server.isNight()) player.send("You can't use this command during day time");
        else {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (!chosenPlayer.isPresent()) player.send(chosenPName + " is unavailable");
            else {
                player.setVote(chosenPlayer.get());

            }
            if (commandConditions(server, player, EnumRole.WOLF, chosenPName)) {
              //  Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
                if (chosenPlayer.isPresent()) chosenPlayer.get().killPlayer();
                else player.send(chosenPName + " is unavailable");
            }
        }


    }
}
