package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each gatherer in the tribe.
 */
public class ExtraPointPerGatherer extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerGatherer(int points){
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of gatherers.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for gatherers
     */
    @Override
    public int onEndOfGame(Tribe tribe){
       return tribe.getTotalGathererCount()*this.points;
    }
}
