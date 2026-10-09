package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * A concrete {@link TileMode} strategy representing an interactive card-picking reward.
 * <p>
 * When a player places their totem on an {@link OfferTile} configured with this mode,
 * they do not receive an instant bonus. Instead, they earn the right to draft a specific
 * number of cards from the board's upper (face-up) and lower (face-down/deck) rows during
 * the offer resolution phase.
 * </p>
 */
public class CardPick implements TileMode {
    private final int upperRowCount;
    private final int lowerRowCount;

    /**
     * Constructs a new CardPick strategy with the specified drafting limits.
     *
     * @param upperRowCount the number of face-up cards the player can pick
     * @param lowerRowCount the number of face-down cards the player can pick
     */
    public CardPick(int upperRowCount, int lowerRowCount){
        this.upperRowCount = upperRowCount;
        this.lowerRowCount = lowerRowCount;
    }

    /**
     * Indicates that this tile mode does not resolve automatically.
     *
     * @return {@code false}, as picking cards requires explicit player choices
     */
    @Override
    public boolean isAutomatic() {
        return false;
    }

    /**
     * Intentionally left blank.
     * <p>
     * Because {@link #isAutomatic()} returns {@code false}, this method should not
     * be triggered. The actual card selection logic is deferred and handled separately
     * by the game state (e.g., during the {@code ResolveOfferTileAction}).
     * </p>
     *
     * @param player the {@link Player} resolving the tile
     */
    @Override
    public void applyAutomaticEffect(Player player) {
        //no automatic effect, the player will have to choose which cards to pick, this will be handled in the ResolveOfferTileAction
    }

    public int getUpperRowCount() {
        return upperRowCount;
    }
    public int getLowerRowCount() {
        return lowerRowCount;
    }
}
