package game;

import game.Characters.Character;
import game.Characters.FortuneTeller;

public enum EnumRole {
    WOLF("Wolf", "Wolves", new Character()),
    VILLAGER("Villager", "Villagers", new Character()),
    FORTUNE_TELLER("Fortune Teller", "Fortune Tellers", new FortuneTeller());

    private final String NAME;
    private final String PLURAL;
    private final Character CHARACTER;

    EnumRole(String name, String plural, Character character) {
        this.NAME = name;
        this.PLURAL = plural;
        this.CHARACTER = character;
    }

    public String toString() {
        return this.NAME;
    }

    public String getPLURAL() {
        return PLURAL;
    }

    public Character getCHARACTER() {
        return CHARACTER;
    }
}
