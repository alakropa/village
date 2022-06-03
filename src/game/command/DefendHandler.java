package game.command;

import game.Characters.Character;
import game.EnumRole;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class DefendHandler implements CommandHandler {

    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String defendedName = Helpers.removeCommand(player.getMessage());
        Character playerCharacter = player.getCharacter();
        EnumRole role = EnumRole.GUARD;

        if (this.commandConditions(server, player, role, defendedName)) {
            Optional<Server.PlayerHandler> defendedPlayer = server.getPlayerByName(defendedName);
            if (defendedPlayer.isPresent()) {
                if (playerCharacter.getPreviousDefend() != null) {
                    playerCharacter.getPreviousDefend().setDefended(true);
                }
                Character defendedCharacter = defendedPlayer.get().getCharacter();
                playerCharacter.setPreviousDefend(defendedCharacter);
                defendedCharacter.setDefended(true);
                //player.setDefend(defendedPlayer.get());

                // server.chat(player.getName(), " voted for " + defendedName);
                //  server.sendUpdateOfVotes();

            } else {
                player.send("Player is unavailable.");
            }
        }
    }
}
