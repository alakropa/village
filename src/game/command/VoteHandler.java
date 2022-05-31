package game.command;

import game.Server.Server;

import java.io.IOException;
import java.util.Optional;

public class VoteHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        String votedPlayerName = player.getMessage().split(" ")[1];
        Optional<Server.PlayerHandler> votedPlayer = server.getClientByName(votedPlayerName);

        while (votedPlayer.isEmpty()) {
            try {
                player.send("Player is unavailable. Vote other player");
                votedPlayerName = player.getIN().readLine().split(" ")[1];
                votedPlayer = server.getClientByName(votedPlayerName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        votedPlayer.get().increaseNumberOfVotes();
    }
}
