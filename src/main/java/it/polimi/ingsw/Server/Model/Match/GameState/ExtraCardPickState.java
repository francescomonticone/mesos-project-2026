package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ActionErrorCode;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ExtraCardPickAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.SkipExtraAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interactive state in which players who own the extra card pick building
 * may take one extra card from the board.
 *
 * <p>Players act in the order they are passed to the constructor.
 * Each player must send either {@link ExtraCardPickAction} or
 * {@link SkipExtraAction}.</p>
 *
 * <p>This state is <strong>immutable</strong>: each valid action produces
 * a new instance with an advanced player index.</p>
 */
public class ExtraCardPickState implements GameState {

    private final List<Player> pendingPlayers;
    private final int currentIndex;

    // Internal counters for the current player's turn within this state
    private final int upperPicksDone;
    private final int lowerPicksDone;

    /**
     * Creates the initial state for the given players.
     *
     * @param pendingPlayers players with the extra pick ability, in order
     */
    public ExtraCardPickState(List<Player> pendingPlayers) {
        this(List.copyOf(pendingPlayers), 0, 0, 0);
    }

    /**
     * Private constructor used to create the next immutable step.
     */
    private ExtraCardPickState(List<Player> pendingPlayers, int currentIndex, int upperPicksDone, int lowerPicksDone) {
        this.pendingPlayers = pendingPlayers;
        this.currentIndex = currentIndex;
        this.upperPicksDone = upperPicksDone;
        this.lowerPicksDone = lowerPicksDone;
    }

    /**
     * This state is interactive — waits for player input.
     *
     * @param model the current match model
     * @return an empty {@code Optional}
     */
    @Override
    public Optional<GameState> onEnter(MatchModel model) {
        return Optional.empty();
    }

    /**
     * Handles a player choosing to take an extra card.
     *
     * @param action the pick action containing row choice and card ID
     * @param model  the current match model
     * @return the next game state
     * @throws IllegalActionException if validation fails
     */
    @Override
    public GameState onExtraCardPick(ExtraCardPickAction action, MatchModel model) throws IllegalActionException {

        Player expected = pendingPlayers.get(currentIndex);

        //extract player from action nickname
        Player player = model.getPlayerByNickname(action.getPlayerNickname())
                .orElseThrow(() -> new IllegalActionException(
                        ActionErrorCode.INVALID_TARGET,
                        "Player not found: " + action.getPlayerNickname()
                ));

        // validate player turn
        if (!player.equals(expected)) {
            throw new IllegalActionException(ActionErrorCode.WRONG_PLAYER,
                    "Not your turn. Expected: " + expected.getNickname());
        }

        // validate row permissions (based on the BuildingEffect)
        validateRowPermission(expected, action.isUpperRow());

        // resolve card ID from the correct row
        List<Card> targetRow = action.isUpperRow() ? model.getBoard().getUpperRow() : model.getBoard().getLowerRow();
        Card chosenCard = resolveCardId(action.getCardId(), targetRow); //extract Card with ID from the targetRow

        // validate Card is pickable (no Event cards)
        if (!chosenCard.isPickable()) {
            throw new IllegalActionException(ActionErrorCode.INVALID_TARGET, "Cannot pick Event cards.");
        }

        // calculate and apply food cost for building case
        int builderDiscount = expected.getTribe().getBuildingDiscount();
        int finalCost = Math.max(0, chosenCard.getFoodCost() - builderDiscount); //getFoodCost is polymorphic

        if (expected.getFoodToken() < finalCost) {
            throw new IllegalActionException(ActionErrorCode.INSUFFICIENT_FOOD,
                    "Not enough food. Need: " + finalCost);
        }

        // apply the transaction
        if (action.isUpperRow()) { //if the card picked is from the upperRow (default in game for now)
            model.getBoard().removeCardFromUpperRow(chosenCard);
        } else {
            model.getBoard().removeCardFromLowerRow(chosenCard);
        }

        if (finalCost > 0) { //only if it's a building pay the card
            expected.removeFood(finalCost);
        }
        //finally the card adds itself in the Tribe
        chosenCard.addToTribe(expected);

        // update the counters
        int nextUpperDone = action.isUpperRow() ? upperPicksDone + 1 : upperPicksDone;
        int nextLowerDone = action.isUpperRow() ? lowerPicksDone : lowerPicksDone + 1;

        // multi picks expansion logic with empty row safety check
        boolean canStillPickUpper = nextUpperDone < expected.getTribe().getExtraUpCardPicksCount() //if the player has other available picks
                && !model.getBoard().getUpperPickable().isEmpty(); //if the row is not empty
        boolean canStillPickLower = nextLowerDone < expected.getTribe().getExtraDownCardPicksCount()
                && !model.getBoard().getLowerPickable().isEmpty();

        if (canStillPickUpper || canStillPickLower) {
            return new ExtraCardPickState(pendingPlayers, currentIndex, nextUpperDone, nextLowerDone);
        }

        // otherwise we advance to the next player or transition to the next state
        return advanceOrTransition();
    }

    /**
     * Handles a player choosing to skip their extra pick.
     *
     * @param action the skip action
     * @param model  the current match model
     * @return the next game state
     * @throws IllegalActionException if it is not this player's turn
     */
    @Override
    public GameState onSkipExtra(SkipExtraAction action, MatchModel model) throws IllegalActionException {
        Player expected = pendingPlayers.get(currentIndex);

        //extract player from action nickname
        Player player = model.getPlayerByNickname(action.getPlayerNickname())
                .orElseThrow(() -> new IllegalActionException(
                        ActionErrorCode.INVALID_TARGET,
                        "Player not found: " + action.getPlayerNickname()
                ));

        if (!player.equals(expected)) {
            throw new IllegalActionException(ActionErrorCode.WRONG_PLAYER,
                    "Not your turn. Expected: " + expected.getNickname());
        }

        return advanceOrTransition(); //player decided strategically to skip
    }



    @Override
    public void requestAction(VirtualView view) throws Exception {
        view.askExtraCard();
    }


    // =================================================================================
    // BOT-FALLBACK LOGIC (DISCONNECTIONS)
    // =================================================================================

    /**
     * Generates a valid random extra card pick (or skip) action for a disconnected player.
     * <p>
     * Strategy:
     * 1. Check which rows the player is allowed to pick from.
     * 2. Try to pick a Character card (cost 0) from an allowed row.
     * 3. Try to pick an affordable Building card from an allowed row.
     * 4. If no affordable cards are found, generate a {@link SkipExtraAction}.
     * </p>
     *
     * @param model the current game model
     * @return a valid {@link ExtraCardPickAction} or {@link SkipExtraAction}
     */
    @Override
    public GameAction generateRandomAction(MatchModel model) {
        Player currentPlayer = pendingPlayers.get(currentIndex);

        int allowedUpper = currentPlayer.getTribe().getExtraUpCardPicksCount();
        int allowedLower = currentPlayer.getTribe().getExtraDownCardPicksCount();

        boolean canPickUpper = upperPicksDone < allowedUpper && !model.getBoard().getUpperPickable().isEmpty();
        boolean canPickLower = lowerPicksDone < allowedLower && !model.getBoard().getLowerPickable().isEmpty();

        // 1. Try to find a valid card in the Upper Row
        if (canPickUpper) {
            Optional<String> pickId = findAffordableBotPick(
                    model.getBoard().getUpperPickable(),
                    currentPlayer
            );
            if (pickId.isPresent()) {
                return new ExtraCardPickAction(currentPlayer.getNickname(), pickId.get(), true);
            }
        }

        // 2. Try to find a valid card in the Lower Row (this is only for future expansions)
        if (canPickLower) {
            Optional<String> pickId = findAffordableBotPick(
                    model.getBoard().getLowerPickable(),
                    currentPlayer
            );
            if (pickId.isPresent()) {
                return new ExtraCardPickAction(currentPlayer.getNickname(), pickId.get(), false);
            }
        }

        // 3. If no affordable cards are found in any allowed row, the bot skips, no extra action
        return new SkipExtraAction(currentPlayer.getNickname());
    }

    /**
     * Helper method to find an affordable card for the bot.
     * It prioritizes characters (cost 0) over buildings.
     */
    private Optional<String> findAffordableBotPick(List<Card> pickableCards, Player bot) {
        // Shuffle to ensure random behavior
        List<Card> shuffled = new ArrayList<>(pickableCards);
        java.util.Collections.shuffle(shuffled);

        // Priority 1: Characters (they cost 0)
        for (Card c : shuffled) {
            if (c.getFoodCost() == 0) {
                return Optional.of(c.getId()); //return a random character
            }
        }

        // Priority 2: Buildings (if affordable)
        int currentFood = bot.getFoodToken();
        int builderDiscount = bot.getTribe().getBuildingDiscount();

        for (Card c : shuffled) {
            int cost = Math.max(0, c.getFoodCost() - builderDiscount); //cost must be positive or zero
            if (currentFood >= cost) { //if the bot can afford this building, it picks it
                return Optional.of(c.getId());
            }
        }

        return Optional.empty(); //no action possible, it will send a skip extra card pick action
    }

    // private helpers

    /**
     * Checks if the player's tribe actually possesses the building effect
     * required to pick from the requested row.
     *
     * @param player   the acting player
     * @param isUpperRow true if attempting to pick from the upper row
     * @throws IllegalActionException if the player lacks the permission
     */
    private void validateRowPermission(Player player, boolean isUpperRow) throws IllegalActionException {

        // reading from all the extraCardPick effect cards owned by the player.
        int allowedUpper = player.getTribe().getExtraUpCardPicksCount();
        int allowedLower = player.getTribe().getExtraDownCardPicksCount();

        if (isUpperRow) {
            if (upperPicksDone >= allowedUpper) {
                throw new IllegalActionException(ActionErrorCode.INVALID_TARGET, "No extra upper picks remaining.");
            }
        } else {
            if (lowerPicksDone >= allowedLower) {
                throw new IllegalActionException(ActionErrorCode.INVALID_TARGET, "No extra lower picks remaining.");
            }
        }
    }

    @Override
    public String getPhaseName() {
        return "EXTRA PICK";
    }

    /**
     * Resolves a card identifier to its corresponding instance.
     *
     * @param id  the unique identifier
     * @param row the board row to search
     * @return the matched Card
     * @throws IllegalActionException if the card is not found
     */
    private Card resolveCardId(String id, List<Card> row) throws IllegalActionException {
        return row.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalActionException(ActionErrorCode.INVALID_TARGET,
                        "Card not found in the specified row: " + id));
    }

    /**
     * Determines the next state based on the pending players list.
     *
     * @return the next {@link GameState}
     */
    private GameState advanceOrTransition() {
        if (currentIndex == pendingPlayers.size() - 1) {
            // All players have completed their extra picks.
            return new EventResolutionState();
        }
        // Advance to the next player, resetting the pick counters (0, 0).
        return new ExtraCardPickState(pendingPlayers, currentIndex + 1, 0, 0);
    }

    @Override
    public Optional<Player> getCurrentPlayer() {
        if (pendingPlayers == null || pendingPlayers.isEmpty() || currentIndex >= pendingPlayers.size()) {
            return Optional.empty();
        }
        return Optional.of(pendingPlayers.get(currentIndex));
    }
}