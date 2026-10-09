package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

import it.polimi.ingsw.Server.Model.Match.GameState.GameAction;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;

/**
 * Action representing a player placing their Totem on an offer tile.
 */
public class PlaceTotemAction implements GameAction {

    private final String playerNickname;
    private final char tileId; // A, B, C ...

    public PlaceTotemAction(String playerNickname, char tileId) {
        this.playerNickname = playerNickname;
        this.tileId = tileId;
    }

    public String getPlayerNickname() { return playerNickname; }
    public char getTileId()   { return tileId; }

    /** Double dispatch: routes to the correct handler on the state. */
    @Override
    public GameState applyTo(GameState state, MatchModel model)
            throws IllegalActionException {
        return state.onPlaceTotem(this, model); //this action will be redirected to the correct method of the TotemPlacementState class
    }
}