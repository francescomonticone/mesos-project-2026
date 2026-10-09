package it.polimi.ingsw.Client.View.ClientBoard;
/**
 * Food gain tile mode.
 *
 * <p>
 * The class implements the visitor pattern through {@code accept}, allowing
 * different operations to be performed on this tile mode without exposing
 * its internal structure.
 * </p>
 *
 * @param food the amount of food gained
 */
public record ClientFoodGain(int food) implements ClientTileMode{
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
