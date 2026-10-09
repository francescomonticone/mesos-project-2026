package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

import it.polimi.ingsw.Server.Model.Match.GameState.GameAction;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;

public class SkipExtraAction implements GameAction {

    private final String playerNickname;

    public SkipExtraAction(String playerNickname) {
        this.playerNickname = playerNickname;
    }

    public String getPlayerNickname() { return playerNickname; }

    @Override
    public GameState applyTo(GameState state, MatchModel model)
            throws IllegalActionException {
        return state.onSkipExtra(this, model);
    }
}
