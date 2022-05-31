package game.command;

import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        System.out.println("command");
        String chosenPName = Helpers.removeCommand(player.getMessage());
        System.out.println("Antes do if");
        if (chosenPName == null) player.send("Unvailble command");
        else if (player.getRole() != EnumRole.WOLF) player.send("Only wolves can use this command");
        else if (!server.isNight()) player.send("You can't use this command during day time");
        else {
            Optional<Server.PlayerHandler> chosenPlayer = server.getPlayerByName(chosenPName);
            if (chosenPlayer.isPresent()) chosenPlayer.get().killPlayer();
            else player.send(chosenPName + " is unavailable");
        }
        System.out.println("Depois do if");
    }
}
