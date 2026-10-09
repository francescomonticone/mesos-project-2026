package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each inventor in the tribe.
 */
public class ExtraPointPerInventor extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerInventor(int points) {
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of inventors.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for inventors
     */
    @Override
    public int onEndOfGame(Tribe tribe){
        return tribe.getTotalInventorCount()*this.points;
    }

}
