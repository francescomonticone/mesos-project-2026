package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Network.DTO.EventResultDTO;
import it.polimi.ingsw.Network.DTO.PlayerDeltaDTO;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResolutionQueue;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The abstract base class representing Event cards in the game.
 * <p>
 * Unlike Characters or Buildings, Event cards are not draftable and are never added to
 * a player's Tribe. Instead, they represent global occurrences or challenges (such as Hunts,
 * Sustenance, or Shamanic Rituals) that trigger at specific phases of the round, evaluating
 * player standings and applying resource changes across all boards simultaneously.
 * </p>
 */
public abstract class EventCard extends Card {
    private final boolean isFinalEvent;

    /**
     * Constructs a base Event card with its fundamental attributes.
     *
     * @param id           the unique identifier for the event card
     * @param era          the {@link Era} to which the event belongs
     * @param minPlayers   the minimum player count required to include this event
     * @param isFinalEvent {@code true} if this is an end-game final event, {@code false} otherwise
     */
    public EventCard(String id, Era era, int minPlayers, boolean isFinalEvent) {
        super(id, era, minPlayers);
        this.isFinalEvent = isFinalEvent;
    }

    public boolean getIsFinalEvent() {
        return isFinalEvent;
    }

    @Override
    public boolean isPickable(){
        return false;
    }

    /**
     * Allows the event card to insert itself into the correct execution queue.
     * <p>
     * By default, the event card is added to the normal event queue. Specific events
     * (such as Sustenance) may override this to route themselves to a different queue.
     * </p>
     *
     * @param queue the {@link EventResolutionQueue} orchestrating event execution
     */
    @Override
    public void addCardToQueue(EventResolutionQueue queue){
        queue.addNormalEvent(this); //by default the Event card is added to the normal event queue, but it can be overridden by sustenance events
    }

    /**
     * Executes the full resolution lifecycle of the event.
     * <p>
     * Uses the Template Method pattern: it calls the abstract {@link #calculate(List)}
     * method to determine resource changes, applies those deltas (food and prestige) directly
     * to each player's board, and packages the outcome into a network-ready DTO.
     * </p>
     *
     * @param players the list of all players in the match affected by the event
     * @param model   the global state of the match, allowing the event outcome to be registered for network syncing
     */
    @Override
    public void handleEventResolution(List<Player> players, MatchModel model) {
        // calculate returns final deltas
        EventResult result = calculate(players);
        //all event cards apply during calculate() all event related buildings effects, so no need to apply them here

        //map to store the deltas for each player to send them over the network and display them in the client
        Map<String, PlayerDeltaDTO> playerDeltas = new HashMap<>();

        // apply deltas to players
        for (Player p : players) {
            int foodDelta     = result.getFoodDeltas().get(p);
            int prestigeDelta = result.getPrestigePointsDeltas().get(p);

            if (foodDelta > 0)      p.addFood(foodDelta);
            else if (foodDelta < 0) p.removeFood(Math.abs(foodDelta));

            if (prestigeDelta > 0)      p.addPrestigePoint(prestigeDelta);
            else if (prestigeDelta < 0) p.removePrestigePoint(Math.abs(prestigeDelta));

            playerDeltas.put(p.getNickname(), new PlayerDeltaDTO(foodDelta, prestigeDelta));
        }

        model.addEventResult(new EventResultDTO(this.getId(), playerDeltas)); //add the EventResultDTO to the current round list in the model
    }

    /**
     * Abstract method to calculate the event's outcome for all players.
     * <p>
     * Concrete event subclasses (e.g., {@code HuntEvent}, {@code SustenanceEvent}) must
     * implement this to apply rule-specific calculations, evaluating shamanic stars,
     * artist bonuses, and building effects.
     * </p>
     *
     * @param players the list of players participating in the event
     * @return an {@link EventResult} containing the calculated food and prestige deltas
     */
    public abstract EventResult calculate (List<Player> players );

    /**
     * Throws an exception, as Event cards cannot be acquired or added to a player's tribe.
     *
     * @param player the player attempting to add the card (unused)
     * @throws UnsupportedOperationException always, as event cards are purely board-driven mechanisms
     */
    @Override
    public void addToTribe(Player player){
        throw new UnsupportedOperationException("Event cards can't be added.");
    }


    /**
     * Places the event card on the board during the initial setup phase.
     * <p>
     * According to the game rules, event cards drawn during the initial setup
     * must be placed exclusively in the upper row.
     * </p>
     *
     * @param board the main game board
     */
    @Override
    public void placeOnBoardDuringSetup(Board board) {
        board.addCardToUpperRow(this);
    }
}
