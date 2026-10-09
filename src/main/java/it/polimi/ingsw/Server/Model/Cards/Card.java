package it.polimi.ingsw.Server.Model.Cards;

import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResolutionQueue;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.List;

/**
 * The abstract base class representing any type of card in the game.
 * <p>
 * This class provides the foundational attributes shared across all cards (ID, Era,
 * and minimum player requirement) and defines the core polymorphic behaviors
 * expected by the game engine, such as board placement, tribe integration, and event handling.
 * </p>
 */
public abstract class Card {
    private final String id;
    private final Era era;
    private final int minPlayers;

    public Card(String id, Era era, int minPlayers) {
        this.id = id;
        this.era = era;
        this.minPlayers = minPlayers;
    }

    /**
     * Adds the card to the specific collection (Characters, Buildings) within a player's tribe.
     * <p>
     * It's implemented by concrete subclasses to ensure the card is routed to
     * the correct list and applies any immediate acquisition effects.
     * </p>
     *
     * @param player the {@link Player} whose tribe is receiving the card
     */
    public abstract void addToTribe(Player player);

    public String getId() {
        return id;
    }

    public Era getEra() {
        return era;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getFoodCost() {return 0;}

    /**
     * Determines whether this card can be actively picked by a player from the board.
     * <p>
     * Must be implemented by concrete subclasses. For example, Characters and Buildings
     * are pickable, while Event cards usually are not.
     * </p>
     *
     * @return {@code true} if the card can be drafted, {@code false} otherwise
     */
    public abstract boolean isPickable(); //default: not pickable

    /** Called during event resolution phase.
     * Default implementation: no-op (all cards that are not events).
     * Event cards override this to implement their resolution logic.
     * */
    public void handleEventResolution(List<Player> players, MatchModel model) {
        // no operations by default
    }

    /**
     * Allows the card to be placed in the correct resolution queue.
     * Empty default implementation to ignore non-event cards (e.g. Buildings).
     */
    public void addCardToQueue(EventResolutionQueue queue) {
        //no operations by default
    }

    /**
     * Places the card on the appropriate row of the board during the initial game setup (Round 0).
     * <p>
     * This method utilizes the Double Dispatch pattern to allow the card to position
     * itself correctly (upper or lower row) without requiring the caller to check its specific type.
     * </p>
     *
     * @param board the main game board where the card will be placed
     */
    public void placeOnBoardDuringSetup(Board board){
        //no operation by default, only overridden by Character and Event cards, Building cards are placed manually in the building rows
    }

}
