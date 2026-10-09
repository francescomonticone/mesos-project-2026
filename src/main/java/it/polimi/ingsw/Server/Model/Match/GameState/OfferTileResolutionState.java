package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraFoodTurnEnd;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Match.Board.OfferTile;
import it.polimi.ingsw.Server.Model.Match.Board.OfferTrack;
import it.polimi.ingsw.Server.Model.Match.Board.TurnOrderTile;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ActionErrorCode;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ResolveOfferTileAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interactive state in which each player resolves the action of their
 * offer tile, picking cards from the upper and/or lower row in
 * left-to-right offer track order.
 *
 * <p>After every player has resolved, transitions to
 * {@link ExtraCardPickState} if at least one player owns the
 * extra card pick building, otherwise to {@link EventResolutionState}.</p>
 *
 * <p>This state is <strong>immutable</strong>: each resolution step
 * returns a new instance with an advanced player index.</p>
 */
public class OfferTileResolutionState implements GameState {

   //players ordered left-to-right are passed from the previous state
    private final List<Player> resolutionOrder;
    //index of the current player, we are waiting for his action
    private final int currentIndex;

    /**
     * Creates the initial resolution state for a round.
     * @param resolutionOrder players in left-to-right offer track order
     */
    public OfferTileResolutionState(List<Player> resolutionOrder) {
        this(List.copyOf(resolutionOrder), 0);
    }

    /**
     * Private constructor for producing the next immutable step.
     */
    private OfferTileResolutionState(List<Player> resolutionOrder, int currentIndex) {
        this.resolutionOrder = resolutionOrder;
        this.currentIndex    = currentIndex;
    }

    /**
     * Initializes the resolution phase for the current player.
     * <p>
     * If the player's chosen offer tile is automatic (e.g., provides only food),
     * it applies the effect, ends the turn, and automatically transitions to the next state.
     * If the tile requires interaction (e.g., picking cards), it waits for the player's action.
     * </p>
     *
     * @param model the current match model
     * @return an {@code Optional} containing the next state if resolved automatically,
     * or an empty {@code Optional} to wait for player input
     */
    //this returns an empty Optional because this state is not automatic except for food gain case
    @Override
    public Optional<GameState> onEnter(MatchModel model) {
        Player current = resolutionOrder.get(currentIndex);
        OfferTile tile  = model.getBoard().getOfferTrack().getTile(current);

        if (tile.isAutomatic()) {
            tile.applyAutomaticEffect(current); //apply automatic tiles (only food but this can be extended in the future with other automatic tiles)

            //turn end resolution
            GameState nextState = endPlayerTurn(current, model); //note that this will trigger advance or transition

            //now go to the next state
            return Optional.of(nextState);
        }
        //if it's not automatic, wait for the player to resolve it with an action (interactive state)
        return Optional.empty();
    }

    /**
     * Handles a player resolving their offer tile action.
     *
     * <p>Validates:</p>
     * <ul>
     *   <li>The acting player is the expected one</li>
     *   <li>Chosen cards match the tile's allowed slots per row</li>
     *   <li>No Event cards are selected (rulebook: cannot take Event cards)</li>
     *   <li>Player has enough food for any chosen Building card</li>
     * </ul>
     *
     * <p>On success:</p>
     * <ul>
     *   <li>Removes chosen cards from the board rows</li>
     *   <li>Applies Building card food costs (minus Builder discount)</li>
     *   <li>Adds all cards to the player's tribe</li>
     *   <li>Moves player's totem to Turn Order tile, applies food bonus</li>
     *   <li>Last space: player pays 1 food or loses 2 PP</li>
     * </ul>
     *
     * @param action the resolution action with chosen cards
     * @param model  the current match model
     * @return the next game state
     * @throws IllegalActionException if any validation fails
     */
    @Override
    public GameState onResolveOfferTile(ResolveOfferTileAction action, MatchModel model) throws IllegalActionException {

        Player expected = resolutionOrder.get(currentIndex);

        //extract player from action nickname
        Player player = model.getPlayerByNickname(action.getPlayerNickname())
                .orElseThrow(() -> new IllegalActionException(
                        ActionErrorCode.INVALID_TARGET,
                        "Player not found: " + action.getPlayerNickname()
                ));

        //check if the action is of the player of that turn, otherwise throw exception
        if (!player.equals(expected))
            throw new IllegalActionException( ActionErrorCode.WRONG_PLAYER, "Not your turn: expected " + expected.getNickname() + ", got " + player.getNickname());

        OfferTrack track = model.getBoard().getOfferTrack();

        //check if chosen cards respect upper/lower constrains (Up and Down number of picks)
        int allowedUpper = track.getTile(expected).getUpperSlots();
        int allowedLower = track.getTile(expected).getLowerSlots();

        //this is to check the case of fewer cards than the allowed, the player cannot pick all the allowed cards
        int maxPossibleUpper = Math.min(allowedUpper, model.getBoard().getUpperRow().size()); //minimum btw allowedUpper and real upper cards
        int maxPossibleLower = Math.min(allowedLower, model.getBoard().getLowerRow().size());

        //you cannot take more cards than allowed
        if (action.getUpperRowIds().size() > maxPossibleUpper) {
            throw new IllegalActionException(ActionErrorCode.INVALID_CARD_COUNT, "Too many upper row cards: allowed " + allowedUpper);
        }
        if (action.getLowerRowIds().size() > maxPossibleLower) {
            throw new IllegalActionException(ActionErrorCode.INVALID_CARD_COUNT, "Too many lower row cards: allowed " + allowedLower );
        }

        //consider only characters
        int requiredUpper = Math.min(allowedUpper, model.getBoard().getUpperRowCharacters().size());
        int requiredLower = Math.min(allowedLower, model.getBoard().getLowerRowCharacters().size());

        //you must take at least the characters available
        if (action.getUpperRowIds().size() < requiredUpper)
            throw new IllegalActionException(ActionErrorCode.INVALID_CARD_COUNT, "You must pick " + requiredUpper + " cards from upper row");
        if (action.getLowerRowIds().size() < requiredLower)
            throw new IllegalActionException(ActionErrorCode.INVALID_CARD_COUNT, "You must pick " + requiredLower + " cards from lower row");


        //convert IDs from the ordered list (we need this because of the order of picks, the player can pick 1 card from upper and then 1 card from lower, but the order of picks is important: e.g., the hunter with arrow and without it)
        List<Card> orderedChoices = new ArrayList<>();

        List<Card> allCards = new ArrayList<>();
        allCards.addAll(model.getBoard().getUpperRow());
        allCards.addAll(model.getBoard().getLowerRow());

        for (String id : action.getOrderedIds())
            orderedChoices.add(resolveCardId(id,allCards)); //search in both rows

        //check if chosen cards are not EventCard in particular

        for (Card c : orderedChoices) {
            if (!c.isPickable()) {
                throw new IllegalActionException(ActionErrorCode.INVALID_TARGET, "Cannot take Event cards from the row");
            }
        }

       //check food for BuildingCards
        int totalFoodCost = computeTotalFoodCost(expected, orderedChoices);

        if (expected.getFoodToken() < totalFoodCost) //if the player doesn't have enough food
            throw new IllegalActionException(ActionErrorCode.INSUFFICIENT_FOOD, "Not enough food: need " + totalFoodCost + ", have " + expected.getFoodToken());

        // remove cards from rows, pay food, add to tribe
        if (totalFoodCost > 0)
            expected.removeFood(totalFoodCost);

        for (Card c : orderedChoices) {
            // Remove from whichever row it was in
            try{
                model.getBoard().removeCardFromUpperRow(c);
            } catch (IllegalStateException e) {
                //not in upper row, try lower row
                model.getBoard().removeCardFromLowerRow(c); //if is not in the lower row too we throw the exception

            }
            // Add to tribe
            c.addToTribe(expected);
        }

        return endPlayerTurn(expected, model); //Turn end resolution
    }


    //private methods

    /**
     * end-of-turn logic: moving the totem to the TurnOrderTile,
     * handling board food bonuses (considering buildings), and applying the last-space penalty.
     * @see ExtraFoodTurnEnd
     */
    private GameState endPlayerTurn(Player player, MatchModel model) {

        //move totem to the OrderTile
        TurnOrderTile turnOrder = model.getBoard().getTurnOrderTile();
        //release offerTile
        model.getBoard().getOfferTrack().releaseTotem(player);
        int turnIndex = turnOrder.placeTotemOnTurnOrderTile(player);
        int baseFoodBonus = turnOrder.getFoodBonusFromPosition(turnIndex);//food bonus list

        player.addTurnOrderBonus(baseFoodBonus); //adds bonus considering also buildings

        if (turnOrder.isLastSpace(turnIndex)) { //apply last space penalty
            player.applyLastSpacePenalty();
        }

        //transition to the next state only if this is completed (All players resolved their actions)
        return advanceOrTransition(model);
    }


    /**
     * Resolves a card identifier to its corresponding {@link Card} instance
     * by searching within the provided board row.
     *
     * @param id  the unique identifier of the card to resolve
     * @param row the board row (upper or lower) to search in
     * @return the {@link Card} matching the given {@code tileId}
     * @throws IllegalActionException with {@link ActionErrorCode#INVALID_TARGET}
     *                                if no card with the given {@code tileId} exists in {@code row}
     */

    private Card resolveCardId(String id, List<Card> row) throws IllegalActionException {
        return row.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalActionException(
                        ActionErrorCode.INVALID_TARGET, "Card not found: " + id));
    }

    /**
     * Computes the total food cost for all Building cards in the ordered action,
     * applying the global Builder discount of the player.
     *
     * @param player         the acting player
     * @param orderedChoices the ordered list of picked cards
     * @return total food to pay (never negative)
     */
    private int computeTotalFoodCost(Player player, List<Card> orderedChoices){
        int builderDiscount = player.getTribe().getBuildingDiscount();
        int totalCost = 0;
        for (Card c : orderedChoices) {
            if(c.getFoodCost() != 0){
                totalCost += Math.max(0, c.getFoodCost() - builderDiscount); //apply builder discount, but cost cannot be negative
            }else{
                CharacterCard ch = (CharacterCard) c;
                builderDiscount += ch.getBuildingDiscount();
            }
        }
        return totalCost;
    }

    /**
     * Decides the next macro-state after all players have resolved.
     *
     * <p>If at least one player owns the ExtraCardPick building,
     * transitions to {@link ExtraCardPickState}.
     * Otherwise, transitions to {@link EventResolutionState}.</p>
     *
     * @param model the current match model
     * @return the next game state
     */
    private GameState resolveNextMacroState(MatchModel model) {
        List<Player> withExtraCardPick = model.getPlayers()
                .stream()
                .filter(p -> p.getTribe().getExtraCardPicksCount() > 0)
                .toList();

        if (!withExtraCardPick.isEmpty()) //if there is at least one player with an extra card pick
            return new ExtraCardPickState(withExtraCardPick);

        return new EventResolutionState();
    }

    /**
     * Determines the next state based on the resolution order.
     * <p>
     * If the current player is the last one in the order, it transitions to the
     * next macro-state. Otherwise, it advances to the next player's turn.
     * </p>
     *
     * @param model the current match model
     * @return the next {@link GameState} (either for the next player or the next macro-phase)
     */
    private GameState advanceOrTransition(MatchModel model) {
        boolean isLast = (currentIndex == resolutionOrder.size() - 1);
        if (isLast) {
            return resolveNextMacroState(model);
        }
        return new OfferTileResolutionState(resolutionOrder, currentIndex + 1); //next player
    }

    // =================================================================================
    // Random action logic(DISCONNECTIONS)
    // =================================================================================

    /**
     * Generates a valid random card pick action for a disconnected player.
     * <p>
     * To guarantee the action is always legal and doesn't crash the state machine:
     * 1. It prioritizes picking Character cards (which are mandatory if available).
     * 2. It attempts to fill any remaining slots with Building cards, but only if the
     * bot can currently afford them (evaluating both food and builder discounts).
     * </p>
     *
     * @param model the current game model
     * @return a valid {@link ResolveOfferTileAction}
     */
    @Override
    public GameAction generateRandomAction(MatchModel model) {
        Player currentPlayer = resolutionOrder.get(currentIndex);
        OfferTrack track = model.getBoard().getOfferTrack();
        OfferTile tile = track.getTile(currentPlayer);

        //extract upper and lower allowed picks
        int allowedUpper = tile.getUpperSlots();
        int allowedLower = tile.getLowerSlots();

        List<String> upperPicks = new ArrayList<>();
        List<String> lowerPicks = new ArrayList<>();
        List<String> orderedPicks = new ArrayList<>();

        // We use a small array to hold state [availableFood, currentDiscount]
        // so it can be updated dynamically as the bot picks cards (e.g. a character might give a discount)
        int[] foodAndDiscount = {
                currentPlayer.getFoodToken(),
                currentPlayer.getTribe().getBuildingDiscount()
        };

        // Process Upper Row
        processBotRowPicks(
                allowedUpper,
                model.getBoard().getUpperRowCharacters(),
                model.getBoard().getUpperPickable(),
                upperPicks,
                orderedPicks,
                foodAndDiscount //helper array for current food and discount state
        );

        // Process Lower Row
        processBotRowPicks(
                allowedLower,
                model.getBoard().getLowerRowCharacters(),
                model.getBoard().getLowerPickable(),
                lowerPicks,
                orderedPicks,
                foodAndDiscount //helper array for current food and discount state
        );

        return new ResolveOfferTileAction(currentPlayer.getNickname(), upperPicks, lowerPicks, orderedPicks);
    }

    /**
     * Helper method for the bot to safely pick cards from a specific row without breaking game rules.
     */
    private void processBotRowPicks(int allowedSlots, List<Card> charactersOnly, List<Card> allPickable,
                                    List<String> rowPicks, List<String> orderedPicks, int[] state) {

        // 1. Mandatory Picks: we must pick characters first to satisfy the "required picks" validation rule.
        List<Card> charsToPick = new ArrayList<>(charactersOnly);
        java.util.Collections.shuffle(charsToPick);

        for (Card c : charsToPick) {//(already shuffled list)
            if (rowPicks.size() >= allowedSlots) break; // Stop if we hit the tile's limit

            rowPicks.add(c.getId());  
            orderedPicks.add(c.getId()); 

            // If the character grants a building discount, update the bot's current discount state
            state[1] += ((CharacterCard) c).getBuildingDiscount(); //the array is characters only so we can cast without errors
        }

        // 2. Optional Picks: If we still have slots left, try picking buildings if affordable
        if (rowPicks.size() < allowedSlots) {
            List<Card> remainingPickables = new ArrayList<>(allPickable);
            remainingPickables.removeAll(charsToPick); // Remove characters we already processed
            java.util.Collections.shuffle(remainingPickables);

            for (Card c : remainingPickables) {
                if (rowPicks.size() >= allowedSlots) break; //as soon we reach the final count of picks we stop

                // Check if the bot can afford this building
                int cost = Math.max(0, c.getFoodCost() - state[1]); //cost must be positive
                if (state[0] >= cost) { //actually can afford
                    state[0] -= cost; // Pay the food, this is virtual since we are just simulating the action generation, but this will be done when the gameState will receive the action

                    rowPicks.add(c.getId());
                    orderedPicks.add(c.getId());
                }
            }
        }
    }

    //for controller info
    /**
     * Returns the player who must resolve their offer tile next.
     *
     * @return the current active player
     */
    public Optional<Player> getCurrentPlayer() {
        return Optional.of(resolutionOrder.get(currentIndex));
    }


    @Override
    public void requestAction(VirtualView view) throws Exception {
        view.askResolveOffer();
    }

    @Override
    public String getPhaseName() {
        return "PICK CARD";
    }
}