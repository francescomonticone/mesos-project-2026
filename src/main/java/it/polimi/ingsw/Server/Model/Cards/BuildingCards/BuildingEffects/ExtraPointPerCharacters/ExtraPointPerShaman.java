package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each shaman in the tribe.
 */
public class ExtraPointPerShaman extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerShaman(int points){
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of shamans.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for shamans
     */
    @Override
    public int onEndOfGame(Tribe tribe){
        return tribe.getTotalShamanCount()*this.points;
    }
}
