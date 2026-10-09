package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Building effect that grants end-game prestige points for each complete set
 * of different character cards in the tribe.
 */
public class PointsPerCompleteSet extends BuildingEffect{
    private final int points; //6
    private final int setSize; //this can be computed by the getter of MatchModel

    public PointsPerCompleteSet(int points, int setSize) {
        this.points = points;
        this.setSize =  setSize;
    }

    /**
     * Returns the prestige points gained at the end of the game from complete sets.
     *
     * @param tribe the tribe owning the building
     * @return the total prestige points gained from complete sets
     */
    @Override
    public int onEndOfGame(Tribe tribe){ //only the player with this card will increase PP
        int sets = tribe.getFullSetsOfCharacter(this.setSize);
        return sets*points;
    }
}
