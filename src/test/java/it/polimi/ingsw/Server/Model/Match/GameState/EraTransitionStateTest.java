package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.CardFactory.BuildingCardFactory;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EraTransitionStateTest {
    private Player p1;
    private Player p2;
    private List<Player> players;

    private MatchModel model;
    private int currentRound;

    private Board board = mock(Board.class, RETURNS_DEEP_STUBS);

    private GameState state;

    @BeforeEach
    void setup(){
        p1 = new Player("Anna");
        p2 = new Player("Bruno");
        players = List.of(p1,p2);

        currentRound = 4;
        model = new MatchModel(1, players.size(), 6);

        model.setBoard(board);
        model.setCurrentRound(currentRound);

        BuildingCardFactory buildingFactory = new BuildingCardFactory();
        model.setBuildingDecksByEra(buildingFactory.createBuildingDecksByEra(players.size()));

        when(board.getTurnOrderTile().getPlayerOrderTopBottom()).thenReturn(players);
    }

    @Test
    @DisplayName("Standard behaviour Era II: shifts building, updates round counter, transition to TotemPlacementState")
    void onEnter_EraII(){
        // Setup
        state = new EraTransitionState(Era.II);

        // Simulate
        Optional<GameState> nextState = state.onEnter(model);

        // Assert and check model interaction
        assertTrue(nextState.isPresent());
        assertInstanceOf(TotemPlacementState.class, nextState.get());
        assertEquals(Era.II, model.getCurrentEra());
        assertEquals(currentRound+1, model.getCurrentRound());


        verify(board, never()).clearLowerRowBuildings();
        verify(board, times(1)).shiftBuildings(any());
        verify(board.getTurnOrderTile(), times(1)).getPlayerOrderTopBottom();
    }

    @Test
    @DisplayName("Standard behaviour Era III: discards lower row's building, shifts building, updates round counter," +
            " transition to TotemPlacementState")
    void onEnter_EraIII(){
        // Setup
        state = new EraTransitionState(Era.III);

        // Simulate
        Optional<GameState> nextState = state.onEnter(model);

        // Assert and check model interaction
        assertTrue(nextState.isPresent());
        assertInstanceOf(TotemPlacementState.class, nextState.get());
        assertEquals(Era.III, model.getCurrentEra());
        assertEquals(currentRound+1, model.getCurrentRound());


        verify(board, times(1)).clearLowerRowBuildings();
        verify(board, times(1)).shiftBuildings(any());
        verify(board.getTurnOrderTile(), times(1)).getPlayerOrderTopBottom();
    }

}