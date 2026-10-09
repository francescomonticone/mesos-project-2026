package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

import it.polimi.ingsw.Server.Model.Match.GameState.GameAction;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * Action sent by a player who owns the extra Card Pick building
 * to take one extra card. Note that in the future we can support an expansion
 * for an extra card pick from the lower row.
 *
 * <p>This action is optional: if the player does not want
 * to use it, they must send {@link SkipExtraAction} instead.</p>
 *
 * <p>Rules constraints:</p>
 * <ul>
 * <li>Only one card can be taken.</li>
 * <li>If a Building card is selected, the player must have
 * enough food to pay its cost.</li>
 * <li>Event cards cannot be picked.</li>
 * </ul>
 */
public class ExtraCardPickAction implements GameAction {

    private final String playerNickname;
    private final String cardId;
    private final boolean isUpperRow;

    /**
     * Creates an extra card pick action.
     *
     * @param playerNickname     the acting player nickname
     * @param cardId     the unique string identifier of the chosen card
     * @param isUpperRow true if picking from the upper row, false for the lower row
     */
    public ExtraCardPickAction(String playerNickname, String cardId, boolean isUpperRow) {
        this.playerNickname = playerNickname;
        this.cardId = cardId;
        this.isUpperRow = isUpperRow;
    }

    /**
     * Retrieves the player nickname who initiated the action.
     *
     * @return the acting {@link Player}
     */
    public String getPlayerNickname() {
        return playerNickname;
    }

    /**
     * Retrieves the unique identifier of the selected card.
     *
     * @return the card's ID as a String
     */
    public String getCardId() {
        return cardId;
    }

    /**
     * Indicates which row the player is picking from.
     *
     * @return {@code true} if picking from the upper row, {@code false} otherwise
     */
    public boolean isUpperRow() {
        return isUpperRow;
    }

    /**
     * Applies this action to the current game state via Double Dispatch.
     *
     * @param state the current {@link GameState}
     * @param model the current {@link MatchModel}
     * @return the next {@link GameState}
     * @throws IllegalActionException if the state rejects the action or validation fails
     */
    @Override
    public GameState applyTo(GameState state, MatchModel model) throws IllegalActionException {
        return state.onExtraCardPick(this, model);
    }
}