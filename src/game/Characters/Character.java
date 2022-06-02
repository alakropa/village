package game.Characters;

import game.EnumRole;

public class Character {
    private boolean alive;
    private EnumRole role;
    private int numberOfVotes;
    private Character previousVote;
    private boolean defended;

    public void killPlayer() {
        this.alive = false;
    }

    public void healPlayer() {
        this.alive = true;
    }

    public void increaseNumberOfVotes() {
        this.numberOfVotes++;
    }

    public EnumRole getRole() {
        return role;
    }

    public Character getPreviousVote() {
        return previousVote;
    }

    public void setPreviousVote(Character previousVote) {
        this.previousVote = previousVote;
    }

    public void decreaseNumberOfVotes() {
        this.numberOfVotes--;
    }

    public int getNumberOfVotes() {
        return numberOfVotes;
    }

    public void setNumberOfVotes(int numberOfVotes) {
        this.numberOfVotes = numberOfVotes;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setRole(EnumRole role) {
        this.role = role;
    }
}
