package game;

public enum EnumRole {
    WOLF("Wolf", "Wolves"),
    VILLAGER("Villager", "Villagers"),
    FORTUNE_TELLER("Fortune Teller", "Fortune Tellers"),
    GUARD("Guard", "Guards");

    private final String NAME;
    private final String PLURAL;

    EnumRole(String name, String plural) {
        this.NAME = name;
        this.PLURAL = plural;
    }

    public String toString() {
        return this.NAME;
    }

    public String getPLURAL() {
        return PLURAL;
    }
}
