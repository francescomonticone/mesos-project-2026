package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.List;
import java.util.Optional;

/**
 * Automatic state that handles the transition to a new Era.
 *
 * <p>Triggered by {@link BoardRegenerationState} when a card from
 * the next Era is revealed while refilling the upper row.</p>
 *
 * <p>Steps executed (in strict order):</p>
 * <ol>
 * <li>Update the global match Era.</li>
 * <li>Discard Building cards from the lower row
 * <em>(only at the start of Era III)</em>.</li>
 * <li>Move Building cards from the upper row to the lower row
 * <em>(Era II and Era III)</em>.</li>
 * <li>Place the new Era's Building cards face-up in the upper row
 * <em>(Era II and Era III)</em>.</li>
 * </ol>
 *
 * <p>After completing all steps, transitions immediately to
 * {@link TotemPlacementState} for the next round.</p>
 */
public class EraTransitionState implements GameState {

    private final Era newEra;

    /**
     * Creates a transition state for the given new Era.
     *
     * @param newEra the Era that was just revealed
     */
    public EraTransitionState(Era newEra) {
        this.newEra = newEra;
    }

    /**
     * Executes all Era transition steps and returns the next state.
     *
     * <p>This state is fully automatic: {@link Optional#of(Object)} is
     * always returned, never {@link Optional#empty()}.</p>
     *
     * @param model the current match model
     * @return {@link TotemPlacementState} for the next round, wrapped in an Optional
     */
    @Override
    public Optional<GameState> onEnter(MatchModel model) {

        Board board = model.getBoard();

        //update the Era in the matchModel
        model.setCurrentEra(newEra);

        // discard lower Building row if the rules require it
        if (newEra.requiresLowerBuildingDiscard()) {
            board.clearLowerRowBuildings();
        }

        // move upper Building cards to the lower row
        // place new Era's Building cards in the upper row.
        // assumes the deck returns all available building cards for setup.
        List<BuildingCard> newEraBuildings = model.getBuildingDecksByEra().get(newEra).drawAll();
        board.shiftBuildings(newEraBuildings); //this shifts and adds

        // update round counter
        model.setCurrentRound(model.getCurrentRound() + 1);

        //retrieve turn order and transition to the next phase
        List<Player> newTurnOrder = board.getTurnOrderTile().getPlayerOrderTopBottom();
        //reset the turn order tile for the next round, after reading the order for the TotemPlacementState
        //board.getTurnOrderTile().reset();
        return Optional.of(new TotemPlacementState(newTurnOrder));
    }
}