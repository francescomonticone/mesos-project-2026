package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Automatic state that executes all end-of-round board operations.
 *
 * <p>Note: totem movement to the Turn Order tile happens earlier,
 * inside {@link OfferTileResolutionState}, immediately after each
 * player resolves their offer tile. This state only handles
 * board cleanup and card drawing.</p>
 *
 * <p>Execution steps:</p>
 * <ol>
 * <li>Discard Characters and Events from the lower row
 * (Building cards stay).</li>
 * <li>Move Characters and Events from the upper row to the lower row
 * (Building cards stay).</li>
 * <li>Refill the upper row drawing ({@code numPlayers + 4}) cards
 * from the main deck.</li>
 * </ol>
 *
 * <p>Transitions to:</p>
 * <ul>
 * <li>{@link EndGameState} if the main deck is exhausted (Fail-safe).</li>
 * <li>{@link EraTransitionState} if a card from the next Era
 * was revealed during the refill.</li>
 * <li>{@link TotemPlacementState} for the next round otherwise,
 * using the Turn Order tile's current top-to-bottom order.</li>
 * </ul>
 */
public class BoardRegenerationState implements GameState {

    /**
     * Executes all board regeneration steps and returns the next state.
     *
     * <p>This state is fully automatic: {@link Optional#of(Object)} is
     * always returned, never {@link Optional#empty()}.</p>
     *
     * @param model the current match model
     * @return the next game state wrapped in {@link Optional#of}
     */
    @Override
    public Optional<GameState> onEnter(MatchModel model) {

        //END GAME CHECK
        if (model.getCurrentRound() == 10) {
            return Optional.of(new EndGameState());
        }

        Board board = model.getBoard();

        //round 0 - initial setup is different
        if(model.getCurrentRound() == 0){
            int lowerRowCards = model.getPlayers().size() + 1; //only triggered during preparation
            while (board.getLowerRow().size() < lowerRowCards && !model.getMainDeck().isEmpty()) {
                Card singleDraw = model.getMainDeck().draw();
                singleDraw.placeOnBoardDuringSetup(board); //use polymorphic method to place card in the correct row
            }

            int targetUpperCards = model.getPlayers().size() + 4;
            int currentUpperCards = board.getUpperRow().size(); //events may be in the upper row
            int cardsNeededForUpper = targetUpperCards - currentUpperCards;

            if (cardsNeededForUpper > 0 && !model.getMainDeck().isEmpty()) {
                List<Card> upperDrawn = model.getMainDeck().drawNextNCard(cardsNeededForUpper); //draw the needed cards for the upper row, if any
                for (Card card : upperDrawn) {
                    board.addCardToUpperRow(card);
                }
            }

            //now add the Building of the ERA I to the upper row, as per setup rules.
            Deck<BuildingCard> era1Deck = model.getBuildingDecksByEra().get(Era.I); //decks are already prepared and shuffled during preparation by the BuildingCardFactory, so we can just draw the whole thing.

            int initialBuildingsCount = era1Deck.getCards().size();
            List<BuildingCard> startingBuildings = new ArrayList<>(era1Deck.drawNextNCard(initialBuildingsCount));

            for (BuildingCard b : startingBuildings) {
                board.addBuildingToUpperRow(b); //add the buildings to the upper row, they will stay there until the end of the first round and then be discarded like normal during the first regeneration.
            }

            //set the round to 1 and read the turn order for the first TotemPlacementState.
            model.setCurrentRound(1);
            List<Player> newTurnOrder = board.getTurnOrderTile().getPlayerOrderTopBottom();

            return Optional.of(new TotemPlacementState(newTurnOrder));
        }
        //STANDARD REGENERATION STATE (ROUND>0)

        // clear lower row: remove Characters and Events.
        board.clearLowerRowNonBuildings();

        // refill upper row: draw (numPlayers + 4) cards.
        // shiftCards handles moving upper non-buildings down and placing new ones up.
        int cardsNeeded = model.getPlayers().size() + 4;
        List<Card> drawn = model.getMainDeck().drawNextNCard(cardsNeeded);
        board.shiftCards(drawn);

        // era transition: check if any drawn card belongs to a later Era.
        Era currentEra  = model.getCurrentEra();
        Era detectedEra = detectNextEra(drawn, currentEra);

        if (detectedEra != currentEra) {
            // EraTransitionState is strictly responsible for updating the Model's Era.
            return Optional.of(new EraTransitionState(detectedEra));
        }

        // normal round progression
        // advance counter and start totem placement.
        model.setCurrentRound(model.getCurrentRound() + 1);

        // read the new turn order (already updated during OfferTileResolutionState).
        List<Player> newTurnOrder = board.getTurnOrderTile().getPlayerOrderTopBottom();
        return Optional.of(new TotemPlacementState(newTurnOrder));
    }


    // private helpers

    /**
     * Scans the newly drawn cards and returns the most advanced Era found.
     * If no card belongs to a later Era, returns {@code currentEra} unchanged.
     *
     * @param drawnCards cards just added to the upper row
     * @param currentEra the Era active before the refill
     * @return the new Era to trigger a transition, or {@code currentEra} if unchanged
     */
    private Era detectNextEra(List<Card> drawnCards, Era currentEra) {
        Era detected = currentEra;
        for (Card card : drawnCards) {
            Era cardEra = card.getEra();
            // check if the card's Era is strictly greater
            if (cardEra != null && cardEra.getValue() > detected.getValue()) {
                detected = cardEra;
            }
        }
        return detected;
    }
}