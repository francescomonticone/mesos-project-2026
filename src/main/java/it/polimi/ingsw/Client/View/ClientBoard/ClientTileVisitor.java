package it.polimi.ingsw.Client.View.ClientBoard;
/**
 * Represents a tile mode that can be visited.
 */
public interface ClientTileVisitor<T> {

    /**
     * visitor method for card pick action
     *
     * @param action card pick action
     */
    T visit(ClientCardPick action);

    /**
     * visitor method for food gain
     *
     * @param action food gain action
     */
    T visit(ClientFoodGain action);
}
