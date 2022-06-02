package game.command;

import game.Characters.Character;
import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VoteHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String votedPName = Helpers.removeCommand(player.getMessage());
        Character playerCharacter = player.getCharacter();
        if (this.commandConditions(server, player, votedPName)) {
            Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPName);
            if (votedPlayer.isPresent()) {
                if (playerCharacter.getPreviousVote() != null) {
                    playerCharacter.getPreviousVote().decreaseNumberOfVotes();
                }
                Character votedPCharaceter = votedPlayer.get().getCharacter();
                playerCharacter.setPreviousVote(votedPCharaceter);
                votedPCharaceter.increaseNumberOfVotes();

                player.setVote(votedPlayer.get());
                server.chat(player.getName(), " voted for " + votedPName);
                server.sendUpdateOfVotes();
            } else {
                player.send("Player is unavailable.");
            }
        }
    }
}
