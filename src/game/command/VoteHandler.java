package game.command;

import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VoteHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (server.isNight()) {
            player.send("You can't use this command now");
            return;
        }
        String votedPlayerName = Helpers.removeCommand(player.getMessage());

        if (votedPlayerName == null) {
            player.send("Unvailble command");
            return;
        }
        Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPlayerName);

        if (votedPlayer.isPresent()) {
            votedPlayer.get().increaseNumberOfVotes();
            player.setVote(votedPlayer.get());

        }
        player.send("Player is unavailable.");
    }
}
