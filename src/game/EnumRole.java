package game;

import game.Characters.Character;
import game.Characters.FortuneTeller;
import game.Characters.Villager;
import game.Characters.Wolf;

public enum EnumRole {
    WOLF("Wolf", "Wolves", new Wolf()),
    VILLAGER("Villager", "Villagers", new Villager()),
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
