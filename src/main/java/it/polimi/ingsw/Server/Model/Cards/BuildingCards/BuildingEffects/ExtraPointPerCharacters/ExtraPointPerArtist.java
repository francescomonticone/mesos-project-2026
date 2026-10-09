package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Effect that awards extra end-game points for each artist in the tribe.
 */
public class ExtraPointPerArtist extends ExtraPointPerCharacter{
    private final int points;

    public ExtraPointPerArtist(int points){
        this.points = points;
    }

    /**
     * Calculates the bonus points at the end of the game based on the number of artists.
     *
     * @param tribe the tribe to evaluate
     * @return the total bonus points for artists
     */
    @Override
    public int onEndOfGame(Tribe tribe){
        return tribe.getTotalArtistCount()*this.points;
    }

}
