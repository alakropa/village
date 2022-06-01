package game.command;

import game.EnumRole;
import game.Server.Server;

public class ListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        if (server.isNight() && !player.getRole().equals(EnumRole.WOLF)) return;
        if (!server.isGameInProgress()) return;
        player.send(server.playersInGame());
    }
}
