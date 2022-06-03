package game.command;

import game.EnumRole;
import game.Server.Server;

public class ListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (!server.isGameInProgress()) {
            player.send("You must start the game first");
            return;
        }
        EnumRole playerRole = player.getRole();
        if (server.isNight() && !playerRole.equals(EnumRole.WOLF)) {
            player.send("Only wolves can use this command at night");
            return;
        }
        player.send(server.playersInGame());
    }
}
