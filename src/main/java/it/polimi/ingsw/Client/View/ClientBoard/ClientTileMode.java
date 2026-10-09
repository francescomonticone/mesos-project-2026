package it.polimi.ingsw.Client.View.ClientBoard;
/**
 * Visitor-accepting tile mode.
 */
public interface ClientTileMode {
    <T> T accept(ClientTileVisitor<T> visitor);
}
