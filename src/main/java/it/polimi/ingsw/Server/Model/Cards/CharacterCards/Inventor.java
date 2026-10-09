package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Represents an Inventor character card in the game.
 * <p>
 * Inventors contribute unique symbols to a player's Tribe, which are crucial for
 * end-game scoring. Rather than providing immediate resource bonuses or discounts,
 * Inventors grant specific {@link InventionIcon}s. The Tribe calculates prestige
 * points at the end of the game based on the variety of distinct inventions collected,
 * as well as the number of identical invention pairs.
 * </p>
 */
public class Inventor extends CharacterCard{
    private final InventionIcon invention;

    public Inventor(int minPlayers, Era era, String id, InventionIcon invention, String type) {
        super(minPlayers, era, id, type);
        this.invention = invention;
    }

    //getter
    @Override
    public InventionIcon getInvention() {
        return invention;
    }

    @Override
    public String getType(){
        return type;
    }
}
