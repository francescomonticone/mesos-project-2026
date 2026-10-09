package it.polimi.ingsw.Server.Model.Match;

import it.polimi.ingsw.Server.Model.Cards.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * A queue system designed to orchestrate the resolution of event cards
 * according to the rules of the game.
 * <p>
 * It separates standard events from sustenance events, ensuring that
 * sustenance events are strictly resolved last, while preserving the
 * original left-to-right spatial order from the board.
 * </p>
 */

public class EventResolutionQueue {
    private final List<Card> normalEvents = new ArrayList<>();
    private final List<Card> sustenanceEvents = new ArrayList<>();

    /**
     * Adds a standard event card to the resolution queue.
     *
     * @param card the standard {@link Card} event to be enqueued
     */
    public void addNormalEvent(Card card) {
        addEventToQueue(card, normalEvents);
    }

    /**
     * Adds a sustenance event card to the resolution queue.
     * <p>
     * Sustenance events are held back and guaranteed to be resolved
     * only after all standard events have been processed.
     * </p>
     *
     * @param card the sustenance {@link Card} event to be enqueued
     */
    public void addSustenanceEvent(Card card) {
        addEventToQueue(card, sustenanceEvents);
    }

    /**
     * Helper method to add events to a queue ordered by Era
     */
    private void addEventToQueue(Card card, List<Card> queue) {
        if(queue.isEmpty()){
            queue.add(card);
        } else {
            int pos = 0;
            for(Card c : queue){
               if(c.getEra().getValue() >= card.getEra().getValue()){ //events with less Era value will always stay before in the queue
                   break;
               }
               pos++;
            }
            queue.add(pos, card);
        }
    }

    /**
     * Resolves all enqueued events in the rule-compliant order.
     * <p>
     * Standard events are executed first, followed strictly by sustenance events.
     * The relative insertion order is preserved within each respective category.
     * </p>
     *
     * @param players the list of players involved in the game
     * @param model the model, it will be used to update EventResultDTO to be sent to the clients
     */
    public void resolveAll(List<Player> players, MatchModel model) {
        //first handle all the normal events, then the sustenance events
        normalEvents.forEach(c -> c.handleEventResolution(players, model));
        //finally handle sustenance events (this is a rule)
        sustenanceEvents.forEach(c -> c.handleEventResolution(players, model));
    }

    // --- METHODS FOR TESTING ---
    public List<Card> getNormalEvents(){return this.normalEvents;}

    public List<Card> getSustenanceEvents(){return this.sustenanceEvents;}
}