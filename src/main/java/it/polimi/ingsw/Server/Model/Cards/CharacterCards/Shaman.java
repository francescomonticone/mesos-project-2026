package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Represents a Shaman character card in the game.
 * <p>
 * Shamans provide shamanic stars that increase a player's standing and influence
 * during specific global events, most notably the Shamanic Ritual. Their star counts
 * are calculated by the player's Tribe to determine prestige gains or mitigate prestige
 * losses when resolving rituals. Their class type ("SHAMAN") also contributes to
 * forming complete character sets for end-game scoring.
 * </p>
 */
public class Shaman extends CharacterCard{
    private final int starPoint;

    public Shaman(int minPlayers, Era era, String id, int starPoint, String type) {
        super(minPlayers, era, id, type);
        this.starPoint = starPoint;
    }

    @Override
    public int getShamanStars() {
        return starPoint;
    }

    public String getType(){
        return type;
    }
}
