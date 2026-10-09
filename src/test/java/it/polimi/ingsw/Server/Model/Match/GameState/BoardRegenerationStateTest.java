package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.CardFactory.BuildingCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.CharacterCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.EventCardFactory;
import it.polimi.ingsw.Server.Model.CardFactory.MainDeckBuilder;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Board.TurnOrderTile;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.GameState.BoardRegenerationState;
import it.polimi.ingsw.Server.Model.Match.GameState.EndGameState;
import it.polimi.ingsw.Server.Model.Match.GameState.EraTransitionState;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.GameState.TotemPlacementState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class BoardRegenerationStateTest {
    private MatchModel matchModel = mock(MatchModel.class);
    private Board board;
    private Deck<Card> mainDeck = mock(Deck.class);
    private TurnOrderTile turnOrderTile = mock(TurnOrderTile.class);

    private Player player1 = mock(Player.class);
    private Player player2 = mock(Player.class);
    private Card card1 = mock(Card.class);
    private Card card2 = mock(Card.class);

    private BoardRegenerationState state;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        state = new BoardRegenerationState();
        players = List.of(player1, player2);
        board  = new Board(players.size());
        board.getTurnOrderTile().setPlayerOrderTopBottom(players);

        // Common stubs needed for almost all scenarios
        when(matchModel.getBoard()).thenReturn(board);
        when(matchModel.getPlayers()).thenReturn(players);
        when(matchModel.getMainDeck()).thenReturn(mainDeck);
    }

    @Test
    @DisplayName("Standard behaviour for round 0: setup board and transition to TotemPlacementState")
    void onEnter_setup(){
        // Setup
        int initialBuildingCount = 1;
        CharacterCardFactory charFactory = new CharacterCardFactory();
        EventCardFactory eventFactory = new EventCardFactory();
        BuildingCardFactory buildingFactory = new BuildingCardFactory();

        Deck<Card> deck = MainDeckBuilder.build(
                charFactory.getAllCards(),
                eventFactory.getAllNonFinalCards(),
                eventFactory.getFinalCards(),
                players.size()
        );

        when(matchModel.getMainDeck()).thenReturn(deck);
        when(matchModel.getCurrentRound()).thenReturn(0);
        when(matchModel.getBuildingDecksByEra()).thenReturn(buildingFactory.createBuildingDecksByEra(players.size()));

        // Simulate
        Optional<GameState> nextState = state.onEnter(matchModel);

        // Assert
        assertEquals(players.size()+1, board.getLowerRow().size());
        assertEquals(players.size()+4+initialBuildingCount, board.getUpperRow().size());
        assertTrue(nextState.isPresent());
        assertInstanceOf(TotemPlacementState.class, nextState.get());
    }

    @Test
    @DisplayName("Normal behaviour: clears board, shifts cards, increments round and transitions to TotemPlacement")
    void onEnter_NormalRound_TransitionsToTotemPlacement() {
        // setup
        int currentRound = 4;
        List<Card> drawnCards = List.of(card1, card2);

        when(mainDeck.drawNextNCard(anyInt())).thenReturn(drawnCards);
        when(mainDeck.isEmpty()).thenReturn(false);
        when(matchModel.getCurrentEra()).thenReturn(Era.I);
        when(card1.getEra()).thenReturn(Era.I);
        when(card2.getEra()).thenReturn(Era.I);

        when(matchModel.getCurrentRound()).thenReturn(currentRound);
        when(turnOrderTile.getPlayerOrderTopBottom()).thenReturn(players);

        // simulate
        Optional<GameState> nextStateOpt = state.onEnter(matchModel);

        // assert result
        assertTrue(nextStateOpt.isPresent());
        assertInstanceOf(TotemPlacementState.class, nextStateOpt.get());

        // check model interaction
        verify(matchModel).setCurrentRound(currentRound + 1);
    }

    @Test
    @DisplayName("Era transition: detects higher Era in drawn cards and transitions to EraTransitionState")
    void onEnter_NewEraDrawn_TransitionsToEraTransitionState() {
        // setup
        List<Card> drawnCards = List.of(card1, card2);
        int currentRound = 3;

        when(mainDeck.drawNextNCard(anyInt())).thenReturn(drawnCards);
        when(mainDeck.isEmpty()).thenReturn(false);
        when(matchModel.getCurrentEra()).thenReturn(Era.I); // Current Era is I
        when(matchModel.getCurrentRound()).thenReturn(currentRound);

        when(card1.getEra()).thenReturn(Era.I);
        when(card2.getEra()).thenReturn(Era.II); // New Era detected!

        // simulate
        Optional<GameState> nextStateOpt = state.onEnter(matchModel);

        // assert result
        assertTrue(nextStateOpt.isPresent());
        assertInstanceOf(EraTransitionState.class, nextStateOpt.get());

        // check model interaction
        verify(matchModel, never()).setCurrentRound(anyInt());
    }
}