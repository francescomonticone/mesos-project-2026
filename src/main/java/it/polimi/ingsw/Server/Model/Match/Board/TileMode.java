package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * Defines the behavior and effects of a specific slot on the Offer Track.
 * <p>
 * This interface uses the Strategy Pattern to encapsulate the different consequences
 * of placing a totem on a tile. A tile mode can either be <b>automatic</b> (e.g., granting
 * a flat food bonus immediately) or <b>interactive</b> (e.g., requiring the player to
 * pick a certain number of cards from the board).
 * </p>
 */
public interface TileMode {

    /**
     * Determines if the tile's effect resolves immediately without requiring further user input.
     *
     * @return {@code true} if the effect is applied automatically upon totem placement,
     * {@code false} if it requires the player to make a choice (e.g., picking cards)
     */
    boolean isAutomatic();

    /**
     * Applies the instant effect of the tile to the specified player.
     * <p>
     * This method should only be called if {@link #isAutomatic()} returns {@code true}.
     * For example, it might directly increase the player's food supply.
     * </p>
     *
     * @param player the {@link Player} who placed their totem on this tile
     */
    void applyAutomaticEffect(Player player);


    /**
     * Retrieves the number of face-up cards (from the upper row) the player is
     * entitled to pick when resolving this tile.
     * <p>
     * By default, this returns 0. It is meant to be overridden exclusively by
     * interactive tile modes (such as a {@code CardPick} implementation).
     * </p>
     *
     * @return the number of upper row picks allowed by this tile mode
     */
    //polymorphic methods to know how many upper picks and lower picks the tile gives, default is 0 for both, only CardPick will override this
    default int getUpperRowCount(){
        return 0;
    }

    /**
     * Retrieves the number of face-down cards (from the lower row / deck) the player is
     * entitled to pick when resolving this tile.
     * <p>
     * By default, this returns 0. It is meant to be overridden exclusively by
     * interactive tile modes (such as a {@code CardPick} implementation).
     * </p>
     *
     * @return the number of lower row picks allowed by this tile mode
     */
    default int getLowerRowCount(){
        return 0;
    }


}
