package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraCardPick;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Shaman;
import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.IllegalActionException;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.PlaceTotemAction;
import it.polimi.ingsw.Server.Model.Match.GameState.Actions.ResolveOfferTileAction;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class OfferTileResolutionStateTest {
    private Player p1 = new Player("Anna");
    private Player p2 = new Player("Bruno");
    private Player p3 = new Player("Clara");
    private Player p4 = new Player("Dario");
    private Player p5 = new Player("Emma");
    private List<Player> players = List.of(p1,p2,p3,p4,p5);

    private MatchModel model;
    private Board board;

    private OfferTileResolutionState state;

    @BeforeEach
    void setup(){
        model = new MatchModel(1, players.size(), 6);
        board = new Board(players.size());
        model.setBoard(board);
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(players);
        for(Player p: players){
            model.addPlayer(p);
        }

        state = new OfferTileResolutionState(players);
    }

    @Test
    @DisplayName("onEnter should transition to EventResolutionState if last player is on an " +
            "automatic tile without ExtraCardPick")
    void onEnter() throws IllegalActionException {
        // Setup
        GameState customState = new TotemPlacementState(List.of(p1));
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(List.of(p1));
//        model.addPlayer(p1);

        PlaceTotemAction customAction = new PlaceTotemAction(p1.getNickname(), 'A');
        customState = customState.onPlaceTotem(customAction, model);

        // Simulate
        Optional<GameState> nextState = customState.onEnter(model);

        // Assert
        assertTrue(nextState.isPresent());
        assertInstanceOf(EventResolutionState.class, nextState.get());
        assertEquals(5, p1.getFoodToken());
    }

    @Test
    @DisplayName("onEnter should transition to ExtraCardPickState if last player is on an " +
            "automatic tile and has ExtraCardPick")
    void onEnter2() throws IllegalActionException {
        // Setup
        GameState customState = new TotemPlacementState(List.of(p1));
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(List.of(p1));
//        model.addPlayer(p1);

        BuildingCard bl = new BuildingCard(2,null, null, 0,0,
                new ExtraCardPick(1,0));
        bl.addToTribe(p1);

        PlaceTotemAction customAction = new PlaceTotemAction(p1.getNickname(), 'A');
        customState = customState.onPlaceTotem(customAction, model);

        // Simulate
        Optional<GameState> nextState = customState.onEnter(model);

        // Assert
        assertTrue(nextState.isPresent());
        assertInstanceOf(ExtraCardPickState.class, nextState.get());
        assertEquals(5, p1.getFoodToken());
    }

    @Test
    @DisplayName("onEnter should transition to OfferTileResolutionState if non-last player is on an " +
            "automatic tile without ExtraCardPick")
    void onEnter3() throws IllegalActionException {
        // Setup
        GameState customState = new TotemPlacementState(List.of(p1,p2));
//        model.addPlayer(p1);
//        model.addPlayer(p2);

        int i = 0;
        for(Player p: List.of(p1,p2)){
            PlaceTotemAction customAction = new PlaceTotemAction(p.getNickname(), (char) ('A'+i));
            i++;
            customState = customState.onPlaceTotem(customAction, model);
        }

        // Simulate
        Optional<GameState> nextState = customState.onEnter(model);

        // Assert
        assertTrue(nextState.isPresent());
        assertInstanceOf(OfferTileResolutionState.class, nextState.get());
        assertEquals(6, p1.getFoodToken());
    }

    @Test
    @DisplayName("onEnter should return empty optional if player isn't on an " +
            "automatic tile")
    void onEnter4() throws IllegalActionException {
        // Setup
        GameState customState = new TotemPlacementState(List.of(p1,p2));
//        model.addPlayer(p1);
//        model.addPlayer(p2);

        int i = 0;
        for(Player p: List.of(p1,p2)){
            PlaceTotemAction customAction = new PlaceTotemAction(p.getNickname(), (char) ('A'+i));
            i++;
            customState = customState.onPlaceTotem(customAction, model);
        }

        // Simulate
        Optional<GameState> nextState = customState.onEnter(model);
        nextState = nextState.get().onEnter(model);

        // Assert
        assertFalse(nextState.isPresent());
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player's nickname on the action is not in" +
            "the match")
    void exception(){
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Gianni", List.of(), List.of(), List.of());

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player's nickname on the action is not the" +
            "expected player's nickname")
    void exception2(){
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Bruno", List.of(), List.of(), List.of());

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes more card in the " +
            "upper row than the allowed ones")
    void exception3() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of("CH_01"), List.of(), List.of("CH_01"));

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes more card in the " +
            "lower row than the allowed ones")
    void exception4() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of(), List.of("CH_01","CH_01"), List.of("CH_01", "CH_01"));

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes more card in the " +
            "lower row than the allowed ones")
    void exception5() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of(), List.of("CH_01","CH_01"), List.of("CH_01", "CH_01"));

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes less card in the " +
            "upper row than the required ones")
    void exception6() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of(), List.of(), List.of());
        CharacterCard initialCard = new Shaman(2, null, "CH_01", 1, null);
        model.getBoard().addCardToUpperRow(initialCard);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('C'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes less card in the " +
            "lower row than the required ones")
    void exception7() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of(), List.of(), List.of());
        CharacterCard initialCard = new Shaman(2, null, "CH_01", 1, null);
        model.getBoard().addCardToLowerRow(initialCard);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes an event card")
    void exception8() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of(), List.of("EV_01"), List.of("EV_01"));
        EventCard initialCard = new SustenanceEvent("EV_01", null, 2, false, 0, 0);
        model.getBoard().addCardToLowerRow(initialCard);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolveOfferTile should throw an IllegalActionException if player takes a building card " +
            "but doesn't have enough food")
    void exception9() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of("BL_01"), List.of(), List.of("BL_01"));
        BuildingCard initialCard = new BuildingCard(2,null, "BL_01", 100, 0, new BonusPoints(1));
        model.getBoard().addBuildingToUpperRow(initialCard);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('C'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate & Assert
        assertThrows(IllegalActionException.class, ()->state.onResolveOfferTile(action,model));
    }

    @Test
    @DisplayName("onResolverOfferTile should transition to OfferTileResolution state if player is not the last one")
    void onResolveOfferTile() throws IllegalActionException {
        // Setup
        ResolveOfferTileAction action = new ResolveOfferTileAction("Anna", List.of("BL_01"), List.of(), List.of("BL_01"));
        BuildingCard initialCard = new BuildingCard(2,null, "BL_01", 1, 0, new BonusPoints(1));
        model.getBoard().addBuildingToUpperRow(initialCard);
        p1.addFood(1);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('C'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate
        GameState nextState = state.onResolveOfferTile(action, model);

        // Assert
        assertEquals(List.of(initialCard), p1.getTribe().getBuildingCardList());
        assertTrue(p1.getTribe().getCharacterCardList().isEmpty());
        assertEquals(3, p1.getFoodToken());
        assertInstanceOf(OfferTileResolutionState.class, nextState);
    }

    @Test
    @DisplayName("onResolverOfferTile should transition to EventResolutionState state if player all tiles action are resolved")
    void onResolveOfferTile2() throws IllegalActionException {
        // Setup
        players = List.of(p1,p2);
        BuildingCard initialUpper = new BuildingCard(2,null, "BL_01", 1, 0, new BonusPoints(1));
        CharacterCard initialLower = new Shaman(2, null, "CH_01", 2, "SHAMAN");

        model.getBoard().addBuildingToUpperRow(initialUpper);
        model.getBoard().addCardToLowerRow(initialLower);
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(players);
        p2.addFood(1);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate
        state = new OfferTileResolutionState(players);
        ResolveOfferTileAction action1 = new ResolveOfferTileAction(p1.getNickname(), List.of(), List.of("CH_01"), List.of("CH_01"));
        GameState nextState = state.onResolveOfferTile(action1, model);
        ResolveOfferTileAction action2 = new ResolveOfferTileAction(p2.getNickname(), List.of("BL_01"), List.of(), List.of("BL_01"));
        nextState = nextState.onResolveOfferTile(action2, model);


        // Assert
        assertEquals(List.of(initialLower), p1.getTribe().getCharacterCardList());
        assertTrue(p1.getTribe().getBuildingCardList().isEmpty());
        assertEquals(3, p1.getFoodToken());
        assertEquals(List.of(initialUpper), p2.getTribe().getBuildingCardList());
        assertTrue(p2.getTribe().getCharacterCardList().isEmpty());
        assertInstanceOf(EventResolutionState.class, nextState);
    }

    @Test
    @DisplayName("onResolverOfferTile should transition to ExtraCardPickState state if player all tiles action are resolved " +
            "and on of them has the ExtraCardPick effect")
    void onResolveOfferTile3() throws IllegalActionException {
        // Setup
        players = List.of(p1,p2);
        BuildingCard initialUpper = new BuildingCard(2,null, "BL_01", 1, 0, new ExtraCardPick(1,0));
        CharacterCard initialLower = new Shaman(2, null, "CH_01", 2, "SHAMAN");

        model.getBoard().addBuildingToUpperRow(initialUpper);
        model.getBoard().addCardToLowerRow(initialLower);
        model.getBoard().getTurnOrderTile().setPlayerOrderTopBottom(players);
        p2.addFood(1);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate
        state = new OfferTileResolutionState(players);
        ResolveOfferTileAction action1 = new ResolveOfferTileAction(p1.getNickname(), List.of(), List.of("CH_01"), List.of("CH_01"));
        GameState nextState = state.onResolveOfferTile(action1, model);
        ResolveOfferTileAction action2 = new ResolveOfferTileAction(p2.getNickname(), List.of("BL_01"), List.of(), List.of("BL_01"));
        nextState = nextState.onResolveOfferTile(action2, model);


        // Assert
        assertEquals(List.of(initialLower), p1.getTribe().getCharacterCardList());
        assertTrue(p1.getTribe().getBuildingCardList().isEmpty());
        assertEquals(3, p1.getFoodToken());
        assertEquals(List.of(initialUpper), p2.getTribe().getBuildingCardList());
        assertTrue(p2.getTribe().getCharacterCardList().isEmpty());
        assertInstanceOf(ExtraCardPickState.class, nextState);
    }

    @Test
    @DisplayName("generateRandomAction test")
    void random() throws IllegalActionException {
        // Setup
        BuildingCard initialUpper = new BuildingCard(2,null, "BL_01", 1, 0, new ExtraCardPick(1,0));
        CharacterCard initialLower = new Shaman(2, null, "CH_01", 2, "SHAMAN");

        model.getBoard().addBuildingToUpperRow(initialUpper);
        model.getBoard().addCardToLowerRow(initialLower);

        GameState prevState = new TotemPlacementState(players);
        int i = 0;
        for(Player p: players){
            PlaceTotemAction place = new PlaceTotemAction(p.getNickname(), (char) ('B'+i));
            i++;
            prevState = prevState.onPlaceTotem(place, model);
        }

        // Simulate
        GameAction result = state.generateRandomAction(model);

        // Assert
        assertInstanceOf(ResolveOfferTileAction.class, result);
        ResolveOfferTileAction check = (ResolveOfferTileAction) result;
        assertEquals(p1.getNickname(), check.getPlayerNickname());
        assertTrue((!check.getLowerRowIds().isEmpty()) || (!check.getUpperRowIds().isEmpty()));
        assertFalse(check.getOrderedIds().isEmpty());
        for(String id: check.getOrderedIds()){
            assertTrue(check.getLowerRowIds().contains(id) || check.getUpperRowIds().contains(id));
        }
    }

}