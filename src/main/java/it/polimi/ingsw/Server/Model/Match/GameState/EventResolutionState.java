package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.EventResolutionQueue;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Automatic state responsible for resolving Event cards at the end of a round.
 * <p>
 * This state enforces two strict Mesos rules:
 * <ul>
 * <li>Sustenance events must always be resolved after standard events.</li>
 * <li>In the final round of the game, events from both the upper and lower rows
 * must be resolved (in contrast to standard rounds where only the lower row is checked).</li>
 * </ul>
 * </p>
 * @see EventResolutionQueue
 * @see SustenanceEvent
 * @see EventCard
 */
public class EventResolutionState implements GameState {

    /**
     * Executes the event resolution phase automatically.
     * <p>
     * It collects eligible cards from the board and passes them to an {@link EventResolutionQueue}.
     * The cards polymorphically insert themselves into the correct resolution order
     * (ignoring the queue if they are not events). Finally, the queue is resolved.
     * </p>
     * * @param model the current match model
     * @return an {@code Optional} containing the next state (either Board Regeneration or End Game)
     */
    @Override
    public Optional<GameState> onEnter(MatchModel model) {

        List<Player> players = model.getPlayers();
        Board board = model.getBoard();
        boolean isFinal = isFinalRound(model); //check if it is the final round

        // Collect all eligible cards based on the current round
        List<Card> cardsToEvaluate = new ArrayList<>();

        // If it is the absolute final round, we also include the upper row
        if (isFinalRound(model)) {
            cardsToEvaluate.addAll(board.getUpperRow()); //appends upperRow in order
        }

        // In all cases, we include the lower row
        cardsToEvaluate.addAll(board.getLowerRow()); //appends lowerRow in order

        // Instantiate the Resolution Queue (Double Dispatch receiver)
        EventResolutionQueue queue = new EventResolutionQueue();

        // Let the cards polymorphically sort themselves into the queue
        // - BuildingCards will do nothing.
        // - Normal EventCards will enqueue in the standard list.
        // - Sustenance EventCards will enqueue in the deferred list.
        for (Card card : cardsToEvaluate) {
            card.addCardToQueue(queue); //the card will know where to add itself
        }

        // Resolve the queue
        // (Normal events first, Sustenance events last)
        queue.resolveAll(players, model);

        // State Routing: generate End game or BoardRegenerationState
        if (isFinal) {
            // Transition to the final scoring phase.
            return Optional.of(new EndGameState());
        } else {
            // Standard round transition
            return Optional.of(new BoardRegenerationState());
        }
    }

    /**
     * Helper method to determine if the match is currently in its final round.
     *
     * @param model the current match model containing round tracker information
     * @return {@code true} if it is the last round of Era III (10 round), {@code false} otherwise
     */
    private boolean isFinalRound(MatchModel model) {
        int currentRound = model.getCurrentRound();
        return currentRound == 10; //fixed value by rules
    }
}
