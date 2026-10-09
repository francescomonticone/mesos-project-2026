package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Represents an Artist character card in the game.
 * <p>
 * Artists contribute to a player's end-game prestige points. As managed by the player's
 * Tribe, collecting pairs of Artist cards yields a significant fixed point bonus.
 * Additionally, their distinct class type ("ARTIST") contributes to forming complete
 * character sets for further end-game scoring.
 * </p>
 */
public class Artist extends CharacterCard{

    private final int artistBonus; //CardFactory initializes it at 1

    public Artist(int minPlayers, Era era, String id, int artistBonus, String type) {
        super(minPlayers, era, id, type);
        this.artistBonus = artistBonus;
    }

    /**
     * Retrieves the artist bonus value of this card.
     * <p>
     * Used by the tribe to count the total number of artists when calculating
     * end-game pairs and prestige points.
     * </p>
     *
     * @return the artist bonus value
     */
    @Override
    public int getArtistBonus() {
        return artistBonus;
    }

    /**
     * Retrieves the specific string classification of this character.
     * <p>
     * Returning exactly "ARTIST" is crucial for the game engine to correctly track
     * distinct character classes when computing bonuses for complete character sets.
     * </p>
     *
     * @return the string literal representing the character type
     */
    @Override
    public String getType(){
        return type; //this is needed to return ARTIST, so the match can count distinct characters in the game to compute the complete set bonuses
    }
}
