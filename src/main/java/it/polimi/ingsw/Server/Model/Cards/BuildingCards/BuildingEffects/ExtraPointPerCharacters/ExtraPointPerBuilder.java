package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each builder in the tribe.
 */
public class ExtraPointPerBuilder extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerBuilder(int points){
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of builders.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for builders
     */
    @Override
    public int onEndOfGame(Tribe tribe){
       return tribe.getTotalBuilderCount()*this.points;
    }
}
