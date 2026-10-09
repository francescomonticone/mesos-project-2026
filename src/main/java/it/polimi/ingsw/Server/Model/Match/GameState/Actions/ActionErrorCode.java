package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

public enum ActionErrorCode {

    WRONG_PLAYER    ("It is not your turn"),
    WRONG_STATE     ("This action is not valid in the current game state"),
    TILE_OCCUPIED   ("The chosen offer tile is already occupied"),
    INSUFFICIENT_FOOD("You do not have enough food for this action"),
    INVALID_TARGET  ("The chosen card or tile is not available"),
    INVALID_CARD_COUNT ("You must select the correct number of cards for each row ");

    private final String defaultMessage;

    ActionErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
