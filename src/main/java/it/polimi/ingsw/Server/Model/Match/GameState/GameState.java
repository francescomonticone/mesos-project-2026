package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ActionErrorCode;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ExtraCardPickAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.PlaceTotemAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ResolveOfferTileAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.SkipExtraAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Network.VirtualView;

import java.util.Optional;

public interface GameState {

    /*
     - Optional.of(nextState)
     - Optional.empty()
     */
    //this will be called for automatic states
    Optional<GameState> onEnter(MatchModel model);

    default GameState onPlaceTotem(PlaceTotemAction action, MatchModel model)
            throws IllegalActionException {
        throw new IllegalActionException(ActionErrorCode.WRONG_STATE);
        //default interface Exception, only TotemPlacementState will override this
    }

    default GameState onResolveOfferTile(ResolveOfferTileAction action, MatchModel model)
            throws IllegalActionException {
        throw new IllegalActionException(ActionErrorCode.WRONG_STATE);
    }

    default GameState onExtraCardPick(ExtraCardPickAction action, MatchModel model)
            throws IllegalActionException {
        throw new IllegalActionException(ActionErrorCode.WRONG_STATE);
    }
    default GameState onSkipExtra(SkipExtraAction action, MatchModel model)
            throws IllegalActionException {
        throw new IllegalActionException(ActionErrorCode.WRONG_STATE);
    }




    default Optional<Player> getCurrentPlayer() {
        return Optional.empty();
    }

    /**
     * Generates a valid random action for the current player.
     * This is utilized as a random action bot when the player whose turn it is has disconnected.
     * <p>
     * By default, it throws an exception because automatic states do not require player actions.
     * Interactive states must override this method to provide valid fallback logic.
     * </p>
     *
     * @param model the game model used to evaluate legal moves
     * @return the randomly generated GameAction
     */
    default GameAction generateRandomAction(MatchModel model) {
        throw new UnsupportedOperationException("Automatic states cannot generate random actions."); //unchecked exception, this will not be triggered for how we implemented the gameState: automatic states getCurrentPlayer returns empty, so the controller will never call generateRandomAction on them
    }


    //only interactive states will override this
    default void requestAction(VirtualView view) throws Exception {
        //no operation
    }

    /**
     * Returns the exact name of the current game phase to be displayed on the UI.
     */
    default String getPhaseName(){
        return "";
    }

}