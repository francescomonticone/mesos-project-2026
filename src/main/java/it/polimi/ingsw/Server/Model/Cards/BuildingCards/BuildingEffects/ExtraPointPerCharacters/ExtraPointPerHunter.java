package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each hunter in the tribe.
 */
public class ExtraPointPerHunter extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerHunter(int points) {
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of hunters.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for hunters
     */
    @Override
    public int onEndOfGame(Tribe tribe){
        return tribe.getTotalHunterCount()*this.points;
    }
}
