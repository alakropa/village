package game.command;

import game.Server.Server;

public class CommandListHandler implements CommandHandler {
    @Override
    public void command(Server server, Server.PlayerHandler player) {
        player.send(Command.getCommandList());
    }
}
