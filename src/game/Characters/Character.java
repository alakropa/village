package game.Characters;

import game.EnumRole;
import game.Server.Server;

import java.util.List;
import java.util.Optional;

public class Character {

    private EnumRole role;
    private int numberOfVotes;
    private Character previousVote;
    private boolean defended;
    private boolean protected;

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


    public void setRole(EnumRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Character{" +
                "role=" + role +
                '}';
    }

    public boolean isDefended() {
        return defended;
    }

    public void setDefended(boolean defended) {
        this.defended = defended;
    }
}
