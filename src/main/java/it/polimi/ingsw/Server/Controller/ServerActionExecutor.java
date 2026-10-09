package it.polimi.ingsw.Server.Controller;

import java.util.List;

/**
 * Interface implemented by the Server (typically the MatchController)
 * to receive and execute decoded actions from the Client DTOs.
 */
public interface ServerActionExecutor {

    /**
     * Executes the Totem placement action on the server.
     *
     * @param nickname the unique identifier of the player
     * @param tileId the identifier of the chosen offer tile
     */
    void executePlaceTotem(String nickname, char tileId);

    /**
     * Executes the Offer Tile resolution action on the server.
     *
     * @param nickname the unique identifier of the player
     * @param upCards the list of card IDs chosen from the upper row
     * @param downCards the list of card IDs chosen from the lower row
     * @param orderedCards the list of card IDs in the order chosen by the player (if applicable)
     */
    void executeResolveOffer(String nickname, List<String> upCards, List<String> downCards, List<String> orderedCards);

    /**
     * Executes the Extra Card Pick action on the server.
     *
     * @param nickname the unique identifier of the player
     * @param cardId the unique identifier of the card selected by the player
     */
    void executeExtraCardPick(String nickname, String cardId, boolean isUpperRow);

    /**
     * Executes the skip extra action on the server.
     *
     * @param nickname the unique identifier of the player
     */
    void executeSkipExtra(String nickname);
}