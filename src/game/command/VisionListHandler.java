package game.command;

import game.EnumRole;
import game.Server.Server;

public class VisionListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        EnumRole role = EnumRole.FORTUNE_TELLER;
        if (!server.isGameInProgress()) player.send("You must start the game first");
        else if (!player.isAlive()) player.send("You are dead");
        else if (!player.getRole().equals(role)) player.send("Only " + role.getPLURAL() + " can use this command");
        player.send("Visions list:");
        player.getVISIONS()
                .forEach((key, value) -> player.send(key + " - " + value));
    }
}
