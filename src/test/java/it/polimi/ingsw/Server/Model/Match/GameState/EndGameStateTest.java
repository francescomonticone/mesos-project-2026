package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Match.GameState.EndGameState;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.TotemColour;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EndGameStateTest {
    private Player player1;
    private Player player2;
    private Player player3;

    private MatchModel match = mock(MatchModel.class);

    private EndGameState state = new EndGameState();

    @Test
    @DisplayName("Normal Behaviour: 1 player has the most prestige point")
    void onEnter_oneWinner(){
        //setup
        player1 = new Player("Anna", new Tribe(List.of(), List.of()),
                0, 1, TotemColour.WHITE, true );
        player2 = new Player("Bruno", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );
        player3 = new Player("Carlo", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );

        when(match.getPlayers()).thenReturn(List.of(player1, player2, player3));

        //simulate
        Optional<GameState> res = state.onEnter(match);

        //assert result
        assertTrue(res.isEmpty());

        //check model interaction
        verify(match).getPlayers();
        verify(match).setWinners(anyList());
        verify(match).setMatchFinished(true);
    }

    @Test
    @DisplayName("Normal Behaviour: 2 player have the most prestige point")
    void onEnter_oneWinner_tieBreak(){
        //setup
        player1 = new Player("Anna", new Tribe(List.of(), List.of()),
                1, 1, TotemColour.WHITE, true );
        player2 = new Player("Bruno", new Tribe(List.of(), List.of()),
                0, 1, TotemColour.WHITE, true );
        player3 = new Player("Carlo", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );

        when(match.getPlayers()).thenReturn(List.of(player1, player2, player3));

        //simulate
        Optional<GameState> res = state.onEnter(match);

        //assert result
        assertTrue(res.isEmpty());

        //check model interaction
        verify(match).getPlayers();
        verify(match).setWinners(anyList());
        verify(match).setMatchFinished(true);
    }

    @Test
    @DisplayName("Normal behaviour: tie between multiple players")
    void onEnter_tie(){
        //setup
        player1 = new Player("Anna", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );
        player2 = new Player("Bruno", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );
        player3 = new Player("Carlo", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true );

        when(match.getPlayers()).thenReturn(List.of(player1, player2, player3));

        //simulate
        Optional<GameState> res = state.onEnter(match);

        //assert result
        assertTrue(res.isEmpty());

        //check model interaction
        verify(match).getPlayers();
        verify(match).setWinners(anyList());
        verify(match).setMatchFinished(true);
    }
}