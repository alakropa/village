package game.command;

import game.Server.Server;

public class StartHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (server.isGameInProgress()) {
            player.send("Game has already started");
            return;
        }
        server.setGameInProgress(true);
        new Thread(() -> {
            try {
                server.startGame();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
