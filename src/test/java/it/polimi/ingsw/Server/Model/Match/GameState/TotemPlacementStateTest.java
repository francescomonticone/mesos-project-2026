package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.PlaceTotemAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TotemPlacementStateTest {
    private Player p1 = new Player("Anna");
    private Player p2 = new Player("Bruno");
    private Player p3 = new Player("Carla");
    private Player p4 = new Player("Dario");
    private Player p5 = new Player("Elena");
    private List<Player> players;

    private MatchModel model;
    private PlaceTotemAction action;

    private Board board;

    private GameState state;

    @BeforeEach
    void setup(){
        players = List.of(p1,p2,p3,p4,p5);
        model = new MatchModel(1, players.size(), 6);
        action = new PlaceTotemAction("Anna", 'A');
        board = new Board(players.size());

        for(Player p: players){
            model.addPlayer(p);
        }
        model.setBoard(board);
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(players);

        state = new TotemPlacementState(model.getBoard().getTurnOrderTile().getPlayerOrderTopBottom());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Constructor with empty list should throw IllegalArgumentException")
    void constructor_test_exception(List<Player> invalidInput){
        assertThrows(IllegalArgumentException.class, () -> {
            new TotemPlacementState(invalidInput);
        });
    }

    @Test
    @DisplayName("onEnter should always return Optional.empty()")
    void onEnterTest(){
        assertEquals(Optional.empty(), state.onEnter(model));
    }


    @Test
    @DisplayName("onPlaceTotem should throw an IllegalActionException when player's nickname on action is not found")
    void exception(){
        // Setup
        PlaceTotemAction customAction = new PlaceTotemAction("Guido", 'A');

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()-> state.onPlaceTotem(customAction,model));
    }

    @Test
    @DisplayName("onPlaceTotem should throw an IllegalActionException when player's nickname on action " +
            "doesn't match expected player")
    void exception2(){
        // Setup
        PlaceTotemAction customAction = new PlaceTotemAction("Bruno", 'A');

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()-> state.onPlaceTotem(customAction,model));
    }

    @Test
    @DisplayName("onPlaceTotem should throw an IllegalActionException when trying to occupy an already" +
            "occupied tile")
    void exception3(){
        // Setup
        model.getBoard().getOfferTrack().placeTotem(p2, 'A');

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()-> state.onPlaceTotem(action,model));
    }

    @Test
    @DisplayName("Valid action from non-last player should advance the state and update the model")
    void onPlaceTotem() throws IllegalActionException {
        // Setup
        Board boardMock = mock(Board.class);

        model.setBoard(boardMock);
        when(boardMock.getOfferTrack()).thenReturn(board.getOfferTrack());
        when(boardMock.getTurnOrderTile()).thenReturn(board.getTurnOrderTile());

        // Simulate
        state = state.onPlaceTotem(action, model);

        // Assert
        verify(boardMock, times(1)).getTurnOrderTile();
        assertInstanceOf(TotemPlacementState.class, state);
    }

    @Test
    @DisplayName("Valid action from last player should transition to OfferTileResolutionState")
    void onPlaceTotem2() throws IllegalActionException {
        // Setup
        Board boardMock = mock(Board.class);

        model.setBoard(boardMock);
        when(boardMock.getOfferTrack()).thenReturn(board.getOfferTrack());
        when(boardMock.getTurnOrderTile()).thenReturn(board.getTurnOrderTile());

        // Simulate
        int i = 0;
        for(Player p: players){
            PlaceTotemAction customAction = new PlaceTotemAction(p.getNickname(), (char) ('A'+i));
            i++;
            state = state.onPlaceTotem(customAction, model);
        }

        // Assert
        verify(boardMock, times(players.size())).getTurnOrderTile();
        assertInstanceOf(OfferTileResolutionState.class, state);
    }

    @Test
    void randomAction(){
        // Setup
        model.setCurrentState(state);

        // Simulate
        GameAction res = state.generateRandomAction(model);

        // Assert
        assertInstanceOf(PlaceTotemAction.class, res);
        PlaceTotemAction check = (PlaceTotemAction) res;
        assertTrue(model.getBoard().getAvailableTotemTiles().contains(check.getTileId()));
    }


}