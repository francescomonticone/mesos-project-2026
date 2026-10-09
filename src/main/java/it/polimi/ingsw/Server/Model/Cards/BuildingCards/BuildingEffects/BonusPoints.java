package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Building effect that grants a fixed number of bonus points at the end of the game.
 */
public class BonusPoints extends BuildingEffect{
    private final int points;

    public BonusPoints(int points) {
        this.points = points;
    }

    /**
     * Returns the fixed end-game bonus points.
     *
     * @param tribe the tribe to evaluate
     * @return the fixed number of bonus points
     */
    @Override
    public int onEndOfGame(Tribe tribe){
        return this.points;
    }
}
