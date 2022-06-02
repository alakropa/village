package game.command;

import game.Characters.Character;
import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class KillHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        Character playerCharacter = player.getCharacter();
        String chosenPName = Helpers.removeCommand(player.getMessage()); //msg do jogador que decidiram matar
        EnumRole role = EnumRole.WOLF;

        if (this.commandConditions(server, player, role, chosenPName)) {
            Optional<Server.PlayerHandler> playerWhoDies = server.getPlayerByName(chosenPName);
            if (playerWhoDies.isPresent()) {
                EnumRole chosenPRole = playerWhoDies.get().getCharacter().getRole();
                if (chosenPRole.equals(EnumRole.WOLF)) {
                    player.send("You can't target a Wolf");
                } else if (player.isAlive()) {
                    player.setVote(playerWhoDies.get()); //voto current
                }
            } else {
                player.send(chosenPName + " is unavailable");
            }
        }
    }
}
