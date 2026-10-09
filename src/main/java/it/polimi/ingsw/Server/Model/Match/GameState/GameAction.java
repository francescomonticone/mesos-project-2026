package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.MatchModel;

public interface GameAction {
    //the action has to applyTo a specific state
    GameState applyTo(GameState state, MatchModel model)
            throws IllegalActionException;

    String getPlayerNickname();
}