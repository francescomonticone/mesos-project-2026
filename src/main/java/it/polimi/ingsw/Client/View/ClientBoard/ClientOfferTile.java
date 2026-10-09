package it.polimi.ingsw.Client.View.ClientBoard;
/**
 * Describes an offer tile with its identifier, player requirement, and mode.
 *
 * @param tileId the tile identifier
 * @param minPlayers the minimum number of players required
 * @param tileMode the tile mode
 */
public record ClientOfferTile(
        char tileId,
        int minPlayers,
        ClientTileMode tileMode
) {
}
