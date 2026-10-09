package it.polimi.ingsw.Client.View.ClientBoard;

/**
 * Card-pick tile mode storing the number of cards in the upper and lower rows.
 *
 * <p>
 * The class implements the visitor pattern through {@code accept}, allowing
 * different operations to be performed on this tile mode without exposing
 * its internal structure.
 * </p>
 *
 * @param upperRowCount number of cards in the upper row
 * @param lowerRowCount number of cards in the lower row
 */

public record ClientCardPick(int upperRowCount, int lowerRowCount) implements ClientTileMode{
    /**
     * Accepts a visitor.
     *
     * @param visitor the visitor
     * @param <T> the return type
     * @return the visitor result
     */
    @Override
    public <T> T accept(ClientTileVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
