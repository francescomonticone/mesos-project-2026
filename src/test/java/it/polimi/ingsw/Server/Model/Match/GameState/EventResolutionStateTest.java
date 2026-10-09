package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.EventResolutionQueue;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventResolutionStateTest {
    private EventResolutionState state = new EventResolutionState();

    private MatchModel model = mock(MatchModel.class);
    private Board board = mock(Board.class);

    private Card card = mock(Card.class);
    private EventCard event = mock(EventCard.class);
    private SustenanceEvent s = mock(SustenanceEvent.class);

    private Player p = mock(Player.class);

    @Test
    @DisplayName("Normal behaviour: gets lower row cards from Board, queues only event card" +
            "resolve events, transition to BoardRegenerationState")
    void onEnter_normal(){
        List<Player> players = List.of(p,p);
        List<Card> row = List.of(card, event, card, card, s);

        when(model.getPlayers()).thenReturn(players);
        when(model.getBoard()).thenReturn(board);
        when(model.getCurrentRound()).thenReturn(1);
        when(board.getLowerRow()).thenReturn(row);

        Optional<GameState> result = state.onEnter(model);

        assertTrue(result.isPresent());
        assertInstanceOf(BoardRegenerationState.class, result.get());

        verify(board, never()).getUpperRow();
        verify(board, times(1)).getLowerRow();
        verify(card, times(3)).addCardToQueue(any());
        verify(event, times(1)).addCardToQueue(any());
        verify(s, times(1)).addCardToQueue(any());
    }

    @Test
    @DisplayName("Final round behaviour: gets lower row cards from Board, queues only event card" +
            "resolve events, transition to EndGameState")
    void onEnter_finalRound(){

        List<Player> players = List.of(p,p);
        List<Card> row = List.of(card, event, card, card, s);

        when(model.getPlayers()).thenReturn(players);
        when(model.getBoard()).thenReturn(board);
        when(model.getCurrentRound()).thenReturn(10);
        when(board.getLowerRow()).thenReturn(row);
        when(board.getUpperRow()).thenReturn(row);

        Optional<GameState> result = state.onEnter(model);

        assertTrue(result.isPresent());
        assertInstanceOf(EndGameState.class, result.get());

        verify(board, times(1)).getUpperRow();
        verify(board, times(1)).getLowerRow();
        verify(card, times(6)).addCardToQueue(any());
        verify(event, times(2)).addCardToQueue(any());
        verify(s, times(2)).addCardToQueue(any());
    }

}