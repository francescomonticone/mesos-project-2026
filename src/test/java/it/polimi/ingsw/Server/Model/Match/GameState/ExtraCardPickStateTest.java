package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraCardPick;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import it.polimi.ingsw.Server.Model.Match.*;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ExtraCardPickAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.SkipExtraAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExtraCardPickStateTest {
    private Player p1 = mock(Player.class);
    private Player p2 = mock(Player.class);

    private MatchModel modelMock = mock(MatchModel.class, RETURNS_DEEP_STUBS);
    private ExtraCardPickAction actionMock = mock(ExtraCardPickAction.class);

    private ExtraCardPickState state;

    @Test
    @DisplayName("onEnter should always return an empty Optional")
    void onEnter(){
        state = new ExtraCardPickState(List.of());

        Optional<GameState> result = state.onEnter(modelMock);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("onExtraCardPick throws IllegalActionException when failing to extract player")
    void exception_test(){
        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.empty());

        assertThrows(IllegalActionException.class, () -> {
            state = new ExtraCardPickState(List.of(p1,p2));
            state.onExtraCardPick(actionMock, modelMock);
        });
    }

    @Test
    @DisplayName("onExtraCardPick throws IllegalActionException when is not the right player's turn")
    void exception_test2(){
        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(p1));

        assertThrows(IllegalActionException.class, () -> {
            state = new ExtraCardPickState(List.of(p2));
            state.onExtraCardPick(actionMock, modelMock);
        });
    }

    @Test
    @DisplayName("onExtraCardPick throws IllegalActionException when player does not posses the ExtraCardPick building")
    void exception_test3(){
        Player player = new Player("Anna", new Tribe(List.of(), List.of()),
                0, 0, TotemColour.WHITE, true);

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));

        assertThrows(IllegalActionException.class, () -> {
            state = new ExtraCardPickState(List.of(player));
            state.onExtraCardPick(actionMock, modelMock);
        });
    }

    @Test
    @DisplayName("onExtraCardPick throws IllegalActionException when player select a card that's not pickable")
    void exception_test4(){
        BuildingCard building = new BuildingCard(2, Era.I, "b", 0, 0, new ExtraCardPick(1, 0));
        EventCard card = new SustenanceEvent("E", Era.I, 2 , false, 0, 9);
        Player player = new Player("Anna", new Tribe(List.of(building), List.of()),
                0, 0, TotemColour.WHITE, true);

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));
        when(actionMock.isUpperRow()).thenReturn(true);
        when(modelMock.getBoard().getUpperRow()).thenReturn(List.of(card));
        when(actionMock.getCardId()).thenReturn("E");

        assertThrows(IllegalActionException.class, () -> {
            state = new ExtraCardPickState(List.of(player));
            state.onExtraCardPick(actionMock, modelMock);
        });
    }

    @Test
    @DisplayName("onExtraCardPick throws IllegalActionException when player does not have food for a building")
    void exception_test5(){
        BuildingCard building = new BuildingCard(2, Era.I, "b", 10, 0, new ExtraCardPick(1, 0));
        EventCard card = new SustenanceEvent("E", Era.I, 2 , false, 0, 9);
        Player player = new Player("Anna", new Tribe(List.of(building), List.of()),
                0, 0, TotemColour.WHITE, true);

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));
        when(actionMock.isUpperRow()).thenReturn(true);
        when(modelMock.getBoard().getUpperRow()).thenReturn(List.of(building));
        when(actionMock.getCardId()).thenReturn("b");

        assertThrows(IllegalActionException.class, () -> {
            state = new ExtraCardPickState(List.of(player));
            state.onExtraCardPick(actionMock, modelMock);
        });
    }

    @Test
    @DisplayName("onExtraCardPick returns new ExtraCardPick if player can choose other cards")
    void onExtraCardPick_normal1() throws IllegalActionException {
        BuildingCard building = new BuildingCard(2, Era.I, "b", 10, 0,
                new ExtraCardPick(2, 0));
        EventCard event = new SustenanceEvent("E", Era.I, 2 , false, 0, 9);
        Player player = new Player("Anna", new Tribe(List.of(building), List.of()),
                11, 0, TotemColour.WHITE, true);
        state = new ExtraCardPickState(List.of(player));

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));
        when(actionMock.isUpperRow()).thenReturn(true);
        when(modelMock.getBoard().getUpperRow()).thenReturn(List.of(building, building, event));
        when(actionMock.getCardId()).thenReturn("b");
        when(modelMock.getBoard().getUpperPickable().isEmpty()).thenReturn(false);

        GameState result = state.onExtraCardPick(actionMock, modelMock);

        assertEquals(1, player.getFoodToken());
        assertEquals(List.of(building, building), player.getTribe().getBuildingCardList());
        assertInstanceOf(ExtraCardPickState.class, result);
        verify(modelMock.getBoard(), times(1)).removeCardFromUpperRow(building);
    }

    @Test
    @DisplayName("onExtraCardPick returns new EventResolutionState if all players completed their picks")
    void onExtraCardPick_normal2() throws IllegalActionException {
        BuildingCard building1 = new BuildingCard(2, Era.I, "b", 10, 0,
                new ExtraCardPick(1, 0));
        BuildingCard building2 = new BuildingCard(2, Era.I, "b2", 10, 0,
                new BonusPoints(10));

        EventCard event = new SustenanceEvent("E", Era.I, 2 , false, 0, 9);
        Player player = new Player("Anna", new Tribe(List.of(building1), List.of()),
                11, 0, TotemColour.WHITE, true);
        state = new ExtraCardPickState(List.of(player));

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));
        when(actionMock.isUpperRow()).thenReturn(true);
        when(modelMock.getBoard().getUpperRow()).thenReturn(List.of(building1, building2, event));
        when(actionMock.getCardId()).thenReturn("b2");
        when(modelMock.getBoard().getUpperPickable().isEmpty()).thenReturn(false);

        GameState result = state.onExtraCardPick(actionMock, modelMock);

        assertEquals(1, player.getFoodToken());
        assertEquals(List.of(building1, building2), player.getTribe().getBuildingCardList());
        assertInstanceOf(EventResolutionState.class, result);
        verify(modelMock.getBoard(), times(1)).removeCardFromUpperRow(building2);
    }

    @Test
    @DisplayName("onExtraCardPick returns new ExtraCardPickState if there's another player with the same effect")
    void onExtraCardPick_normal3() throws IllegalActionException {
        BuildingCard building1 = new BuildingCard(2, Era.I, "b", 10, 0,
                new ExtraCardPick(1, 0));
        BuildingCard building2 = new BuildingCard(2, Era.I, "b2", 10, 0,
                new BonusPoints(10));

        EventCard event = new SustenanceEvent("E", Era.I, 2 , false, 0, 9);
        Player player = new Player("Anna", new Tribe(List.of(building1), List.of()),
                11, 0, TotemColour.WHITE, true);
        state = new ExtraCardPickState(List.of(player, player));

        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));
        when(actionMock.isUpperRow()).thenReturn(true);
        when(modelMock.getBoard().getUpperRow()).thenReturn(List.of(building1, building2, event));
        when(actionMock.getCardId()).thenReturn("b2");
        when(modelMock.getBoard().getUpperPickable().isEmpty()).thenReturn(false);

        GameState result = state.onExtraCardPick(actionMock, modelMock);

        assertEquals(1, player.getFoodToken());
        assertEquals(List.of(building1, building2), player.getTribe().getBuildingCardList());
        assertInstanceOf(ExtraCardPickState.class, result);
        verify(modelMock.getBoard(), times(1)).removeCardFromUpperRow(building2);
    }

    @Test
    @DisplayName("onSkipExtra normal behaviour with 1 player")
    void onSkipExtra() throws IllegalActionException {
        BuildingCard building1 = new BuildingCard(2, Era.I, "b", 10, 0,
                new ExtraCardPick(1, 0));
        Player player = new Player("Anna", new Tribe(List.of(building1), List.of()),
                11, 0, TotemColour.WHITE, true);
        SkipExtraAction skipAction = new SkipExtraAction(player.getNickname());

        state = new ExtraCardPickState(List.of(player));
        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));

        GameState result = state.onSkipExtra(skipAction, modelMock);

        assertInstanceOf(EventResolutionState.class, result);
    }

    @Test
    @DisplayName("onSkipExtra normal behaviour with 2 player")
    void onSkipExtra2() throws IllegalActionException {
        BuildingCard building1 = new BuildingCard(2, Era.I, "b", 10, 0,
                new ExtraCardPick(1, 0));
        Player player = new Player("Anna", new Tribe(List.of(building1), List.of()),
                11, 0, TotemColour.WHITE, true);
        SkipExtraAction skipAction = new SkipExtraAction(player.getNickname());

        state = new ExtraCardPickState(List.of(player, player));
        when(modelMock.getPlayerByNickname(any())).thenReturn(Optional.of(player));

        GameState result = state.onSkipExtra(skipAction, modelMock);

        assertInstanceOf(ExtraCardPickState.class, result);
    }

    @Test
    @DisplayName("Bot can pick a card")
    void random(){
        // Setup
        Player player1 = new Player("Anna");
        List<Player> players = List.of(player1);

        Card genericCard = mock(Card.class);
        BuildingCard bl = new BuildingCard(2,null,null, 0,0,
                new ExtraCardPick(1,0));
        bl.addToTribe(player1);

        MatchModel model = new MatchModel(1, players.size(), 6);
        Board board  = new Board(players.size());
        board.addCardToUpperRow(genericCard);
        model.setBoard(board);

        state = new ExtraCardPickState(List.of(player1));

        when(genericCard.getFoodCost()).thenReturn(0);
        when(genericCard.isPickable()).thenReturn(true);
        when(genericCard.getId()).thenReturn("GA");

        //Simulate
        GameAction nextAction = state.generateRandomAction(model);

        // Assert
        assertInstanceOf(ExtraCardPickAction.class, nextAction);
        ExtraCardPickAction check = (ExtraCardPickAction) nextAction;
        assertEquals("GA", check.getCardId());
        assertEquals(player1.getNickname(), check.getPlayerNickname());
        assertTrue(check.isUpperRow());
    }

    @Test
    @DisplayName("Bot can't pick a card")
    void randomSkip(){
        // Setup
        Player player1 = new Player("Anna");
        List<Player> players = List.of(player1);

        Card genericCard = mock(Card.class);
        BuildingCard bl = new BuildingCard(2,null,null, 0,0,
                new ExtraCardPick(1,0));
        bl.addToTribe(player1);

        MatchModel model = new MatchModel(1, players.size(), 6);
        Board board  = new Board(players.size());
        model.setBoard(board);

        state = new ExtraCardPickState(List.of(player1));

        when(genericCard.getFoodCost()).thenReturn(0);
        when(genericCard.isPickable()).thenReturn(true);
        when(genericCard.getId()).thenReturn("GA");

        //Simulate
        GameAction nextAction = state.generateRandomAction(model);

        // Assert
        assertInstanceOf(SkipExtraAction.class, nextAction);
        SkipExtraAction check = (SkipExtraAction) nextAction;
        assertEquals(player1.getNickname(), check.getPlayerNickname());
    }
}