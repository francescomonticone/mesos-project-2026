package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Match.Board.OfferTrack;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ActionErrorCode;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.PlaceTotemAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.List;
import java.util.Optional;

/**
 * Interactive state: each player places their Totem on a free offer tile,
 * following the order defined by the Turn Order tile (top to bottom).
 *
 * <p>This state is <strong>immutable</strong>: each valid placement produces
 * a new instance with an advanced player index, avoiding shared mutable state
 * and making concurrent access safe.</p>
 *
 * <p>Transitions to {@link OfferTileResolutionState} once all players
 * have placed their Totem.</p>
 */
public class TotemPlacementState implements GameState {

    private final List<Player> placementOrder; //list of ordered players

    // Index of the player who must place now.
    private final int currentIndex;


    public TotemPlacementState(List<Player> placementOrder) {
        if (placementOrder == null || placementOrder.isEmpty()) {
            throw new IllegalArgumentException("Placement order cannot be null or empty");
        }
        this(List.copyOf(placementOrder), 0); //defensive copy, start from player in position 0
    }

    /**
     * Private constructor for producing the next immutable step.
     *
     * @param placementOrder immutable player list
     * @param currentIndex   index of the player who acts next
     */
    private TotemPlacementState(List<Player> placementOrder, int currentIndex) {
        this.placementOrder = placementOrder;
        this.currentIndex   = currentIndex;
    }

    //this return an empty Optional because this state is not automatic
    @Override
    public Optional<GameState> onEnter(MatchModel model) {
        System.out.println("[TotemPlacementState] Entering phase...");
        return Optional.empty();
    }

    /**
     * Handles a totem placement action.
     *
     * <p>Validates that:</p>
     * <ul>
     *   <li>The acting player is the expected one in placement order</li>
     *   <li>The chosen offer tile is free</li>
     * </ul>
     *
     * <p>On success, either advances to the next player (returning a new
     * {@code TotemPlacementState}) or transitions to
     * {@link OfferTileResolutionState} when all players have placed.</p>
     *
     * @param action the placement action from the player
     * @param model  the current match model
     * @return the next game state
     * @throws IllegalActionException if it's not the player's turn
     *                                or the chosen tile is already occupied
     */

    @Override
    public GameState onPlaceTotem(PlaceTotemAction action, MatchModel model) throws IllegalActionException {

        Player expected = placementOrder.get(currentIndex); //pick the player in position currentIndex

        //extract the player from the action nickname
        Player player = model.getPlayerByNickname(action.getPlayerNickname())
                .orElseThrow(() -> new IllegalActionException(
                        ActionErrorCode.INVALID_TARGET,
                        "Player not found: " + action.getPlayerNickname()
                ));

        //check if the action is of the player of that turn
        if (!player.equals(expected))
            throw new IllegalActionException(
                    ActionErrorCode.WRONG_PLAYER, "Not your turn: expected " + expected.getNickname() + ", got " + player.getNickname());

        OfferTrack offerTrack = model.getBoard().getOfferTrack();

        //check if the tile is free before
        if (!offerTrack.isTileFree(action.getTileId()))
            throw new IllegalActionException(ActionErrorCode.TILE_OCCUPIED, "Offer tile '" + action.getTileId() + "' is already occupied");

        // Apply the placeTotem on offerTrack
        offerTrack.placeTotem(expected, action.getTileId());

        model.getBoard().getTurnOrderTile().removeFromTurnOrderTile(currentIndex); //move the totem on the Turn Order tile as well, in sync with the offer track

        // Transition to the next state only if all players have placed their totem
        boolean allPlaced = (currentIndex == placementOrder.size() - 1);

        if (allPlaced) {
            //action resolution follows left-to-right offer track order
            List<Player> leftToRight = offerTrack.getPlayersLeftToRight();
            return new OfferTileResolutionState(leftToRight); //next state
        }

        // Next player's turn → new immutable instance, use the private constructor
        return new TotemPlacementState(placementOrder, currentIndex + 1);
    }

    // =================================================================================
    // Random action logic(DISCONNECTIONS)
    // =================================================================================
    /**
     * Generates a random valid Totem placement action for the disconnected current player
     * by picking a random available totem tile on the board.
     *
     * @param model the current game model
     * @return a {@link PlaceTotemAction} on a randomly chosen valid tile
     */
    @Override
    public GameAction generateRandomAction(MatchModel model) {
        Player currentPlayer = null;
        if(model.getCurrentState().getCurrentPlayer().isPresent()) {
            currentPlayer = model.getCurrentState().getCurrentPlayer().get();
        }
        //Retrieve all valid positions where a totem can be placed
        List<Character> availableTiles = model.getBoard().getAvailableTotemTiles();

        //Pick a random valid tile
        char randomTile = availableTiles.get(new java.util.Random().nextInt(availableTiles.size())); //picks a random tile by extracting a random number

        // Return the action (the exact same one a real client would create)
        return new PlaceTotemAction(currentPlayer.getNickname(), randomTile);
    }

    //methods for info for the Controller
    /**
     * Returns the player who must place their Totem next.
     *
     * @return the current active player
     */
    public Optional<Player> getCurrentPlayer() {
        return Optional.of(placementOrder.get(currentIndex));
    }


    @Override
    public void requestAction(VirtualView view) throws Exception {
        view.askPlaceTotem();
    }


    @Override
    public String getPhaseName() {
        return "PLACE TOTEM";
    }
}
