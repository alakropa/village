package game.command;

import game.Helpers;
import game.Server.Server;

import java.util.Optional;

public class VoteHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String votedPlayerName = Helpers.removeCommand(player.getMessage());
        if (!server.isGameInProgress()) player.send("Game hasn't started yet");
        else if (server.isNight()) player.send("You can't use this command now");
        else if (votedPlayerName == null) player.send("Unvailble command");
        else {
            Optional<Server.PlayerHandler> votedPlayer = server.getPlayerByName(votedPlayerName);

            if (votedPlayer.isPresent()) {
                votedPlayer.get().increaseNumberOfVotes();
                player.setVote(votedPlayer.get());
                server.chat(player.getName(), " voted for " + votedPlayerName);
                server.sendUpdateOfVotes();
            } else {
                player.send("Player is unavailable.");
            }
        }
    }
}
