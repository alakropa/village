package game.Characters;

import java.util.HashMap;

public class FortuneTeller extends Character {
    private final HashMap<String, Boolean> VISIONS;
    private boolean usedVision;

    public FortuneTeller() {
        this.VISIONS = new HashMap<>();
    }

    public HashMap<String, Boolean> getVISIONS() {
        return VISIONS;
    }

    public void addVisions(String playerName, Boolean isWolf) {
        this.VISIONS.put(playerName, isWolf);
    }

    public boolean hasUsedVision() {
        return usedVision;
    }

    public void setUsedVision(boolean usedVision) {
        this.usedVision = usedVision;
    }
}
