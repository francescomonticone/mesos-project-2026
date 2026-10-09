package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

/**
 * Thrown when a player attempts an action that is not legal
 * in the current game state or game rules.
 */

public class IllegalActionException extends Exception {

    private final ActionErrorCode code; //this code is one of the ENUM error codes

    //custom message
    public IllegalActionException(ActionErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    //default Message of the Enum ActionErrorCode
    public IllegalActionException(ActionErrorCode code) {
        super(code.getDefaultMessage());
        this.code = code;
    }
}