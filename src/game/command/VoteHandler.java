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
            player.send("Unavailable command");
            return;
        }
        Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPlayerName); //guarda um playerHander, não só o nome

        if (votedPlayerName.equals(player.getName())) {
            player.send("You can't vote on yourself!");
            return;
        }

        if (votedPlayer.isPresent()) {
            if (player.getPreviousVote() != null) {
                player.getPreviousVote().decreaseNumberOfVotes();
            }
            player.setPreviousVote(votedPlayer.get());
            votedPlayer.get().increaseNumberOfVotes();
            player.setVote(votedPlayer.get());
            server.chat(player.getName(), " voted for " + votedPlayerName);
            server.sendUpdateOfVotes();
        } else {
            player.send("Player is unavailable.");
        }
    }
}
