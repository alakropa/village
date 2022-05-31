package game.command;

import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VoteHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String votedPlayerName = Helpers.removeCommand(player.getMessage());
        Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPlayerName);

        if (votedPlayer.isEmpty()) player.send("Player is unavailable.");
        else {
            votedPlayer.get().increaseNumberOfVotes();
            player.setVote(votedPlayer.get());
        }
    }
}
