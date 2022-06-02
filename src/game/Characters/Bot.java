package game.Characters;

public class Bot extends Character {
    private final String NAME;
    private static int botNumber;

    public Bot() {
        this.NAME = "Bot" + ++botNumber;
    }

    public String getNAME() {
        return NAME;
    }
}
