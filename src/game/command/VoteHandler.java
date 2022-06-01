package game.command;

import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VoteHandler implements CommandHandler {
    public void command(Server server, Server.PlayerHandler player) {
        String votedPName = Helpers.removeCommand(player.getMessage());
        if (this.commandConditions(server, player, votedPName)) {
            Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPName);
            if (votedPlayer.isPresent()) {
                if (player.getPreviousVote() != null) {
                    player.getPreviousVote().decreaseNumberOfVotes();
                }
                player.setPreviousVote(votedPlayer.get());
                (votedPlayer.get()).increaseNumberOfVotes();
                player.setVote(votedPlayer.get());
                server.chat(player.getName(), " voted for " + votedPName);
                server.sendUpdateOfVotes();
            } else {
                player.send("Player is unavailable.");
            }
        }

    }
}
