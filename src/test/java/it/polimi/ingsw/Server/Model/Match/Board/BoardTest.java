package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BoardTest {

    private Board board;
    private final int numPlayers = 4;

    @BeforeEach
    void setUp() {
        board = new Board(numPlayers);
    }


    @Test
    @DisplayName("Default constructor test")
    void constructor() {
        assertNotNull(board.getUpperRow(), "Upper row should not be null");
        assertNotNull(board.getLowerRow(), "Lower row should not be null");
        assertNotNull(board.getTurnOrderTile(), "TurnOrderTile should be initialized");
        assertNotNull(board.getOfferTrack(), "OfferTrack should be initialized");
        assertTrue(board.getUpperRow().isEmpty());
        assertTrue(board.getLowerRow().isEmpty());
    }

    @Test
    @DisplayName("Standard shiftCard method behaviour")
    void shift1() {
        // Setup
        Card initialUpperCard = mock(Card.class);
        Card initialLowerCard = mock(Card.class);
        BuildingCard initialUpperBuilding = mock(BuildingCard.class);
        board.addCardToUpperRow(initialUpperCard);
        board.addBuildingToUpperRow(initialUpperBuilding);
        board.addCardToLowerRow(initialLowerCard);

        List<Card> newCards = new ArrayList<>();
        Card newCard1 = mock(Card.class);
        Card newCard2 = mock(Card.class);
        newCards.add(newCard1);
        newCards.add(newCard2);

        // Simulate
        board.shiftCards(newCards);

        // Assert
        // Old lower row should be cleared. Old upper row moves to lower row.
        assertEquals(1, board.getLowerRow().size());
        assertTrue(board.getLowerRow().contains(initialUpperCard));
        assertFalse(board.getLowerRow().contains(initialLowerCard));
        assertFalse(board.getLowerRow().contains(initialUpperBuilding));

        // Upper row should contain the new cards
        assertEquals(3, board.getUpperRow().size());
        assertTrue(board.getUpperRow().contains(newCard1));
        assertTrue(board.getUpperRow().contains(newCard2));
        assertTrue(board.getUpperRow().contains(initialUpperBuilding));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("shiftCards method with null or empty parameter should throw IllegalArgumentException")
    void shift2(List<Card> invalidCards) {
        assertThrows(IllegalArgumentException.class, () -> board.shiftCards(invalidCards),
                "Should throw IllegalArgumentException when shifting null or empty card list");
    }

    @Test
    @DisplayName("Standard shiftBuildings method behaviour")
    void shift3(){
        // Setup
        Card initialUpperCard = mock(Card.class);
        Card initialLowerCard = mock(Card.class);
        BuildingCard initialUpperBuilding = mock(BuildingCard.class);
        BuildingCard initialLowerBuilding = mock(BuildingCard.class);

        Board customBoard = new Board(List.of(initialUpperCard), List.of(initialLowerCard),
                List.of(initialUpperBuilding), List.of(initialLowerBuilding),
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        BuildingCard newBuilding1 = mock(BuildingCard.class);
        BuildingCard newBuilding2 = mock(BuildingCard.class);
        List<BuildingCard> newCards = new ArrayList<>(List.of(newBuilding1, newBuilding2));

        // Simulate
        customBoard.shiftBuildings(newCards);

        // Assert
        assertEquals(2, customBoard.getLowerRow().size());
        assertFalse(customBoard.getLowerRow().contains(initialLowerBuilding));
        assertFalse(customBoard.getLowerRow().contains(initialUpperCard));
        assertTrue(customBoard.getLowerRow().contains(initialLowerCard));
        assertTrue(customBoard.getLowerRow().contains(initialUpperBuilding));

        assertEquals(3, customBoard.getUpperRow().size());
        assertTrue(customBoard.getUpperRow().contains(newBuilding1));
        assertTrue(customBoard.getUpperRow().contains(newBuilding2));
        assertTrue(customBoard.getUpperRow().contains(initialUpperCard));
        assertFalse(customBoard.getUpperRow().contains(initialUpperBuilding));

    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("shiftBuildings method with null or empty parameter should throw IllegalArgumentException")
    void shift4(List<BuildingCard> invalidCards) {
        assertThrows(IllegalArgumentException.class, () -> board.shiftBuildings(invalidCards),
                "Should throw IllegalArgumentException when shifting null or empty card list");
    }

    @Test
    @DisplayName("Standard clearLowerRowNonBuildings method behaviour")
    void clear1() {
        // Setup
        Card initialLowerCard = mock(Card.class);
        BuildingCard initialLowerBuilding = mock(BuildingCard.class);
        List<Card> lowerRow = new ArrayList<>(List.of(initialLowerCard));
        List<Card> lowerBuildings = new ArrayList<>(List.of(initialLowerBuilding));
        Board customBoard = new Board(new ArrayList<>(), lowerRow, new ArrayList<>(), lowerBuildings,
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        // Simulate
        customBoard.clearLowerRowNonBuildings();

        // Assert
        assertEquals(1, customBoard.getLowerRow().size());
        assertTrue(customBoard.getLowerRow().contains(initialLowerBuilding));
        assertFalse(customBoard.getLowerRow().contains(initialLowerCard));
    }

    @Test
    @DisplayName("Standard clearLowerRowBuildings method behaviour")
    void clear2() {
        // Setup
        Card initialLowerCard = mock(Card.class);
        BuildingCard initialLowerBuilding = mock(BuildingCard.class);
        List<Card> lowerRow = new ArrayList<>(List.of(initialLowerCard));
        List<Card> lowerBuildings = new ArrayList<>(List.of(initialLowerBuilding));
        Board customBoard = new Board(new ArrayList<>(), lowerRow, new ArrayList<>(), lowerBuildings,
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        // Simulate
        customBoard.clearLowerRowBuildings();

        // Assert
        assertEquals(1, customBoard.getLowerRow().size());
        assertFalse(customBoard.getLowerRow().contains(initialLowerBuilding));
        assertTrue(customBoard.getLowerRow().contains(initialLowerCard));
    }

    @Test
    @DisplayName("Standard removeCardFromUpperRow method behaviour when called on a existent non-building Card")
    void remove1() {
        // Setup
        Card card1 = mock(Card.class);
        BuildingCard building1 = mock(BuildingCard.class);
        board.addCardToUpperRow(card1);
        board.addBuildingToUpperRow(building1);

        // Simulate
        board.removeCardFromUpperRow(card1);

        // Assert
        assertEquals(1, board.getUpperRow().size());
        assertTrue(board.getUpperRow().contains(building1));
        assertFalse(board.getUpperRow().contains(card1));
    }

    @Test
    @DisplayName("Standard removeCardFromUpperRow method behaviour when called on a existent building Card")
    void remove2() {
        // Setup
        Card card1 = mock(Card.class);
        BuildingCard building1 = mock(BuildingCard.class);
        board.addCardToUpperRow(card1);
        board.addBuildingToUpperRow(building1);

        // Simulate
        board.removeCardFromUpperRow(building1);

        // Assert
        assertEquals(1, board.getUpperRow().size());
        assertFalse(board.getUpperRow().contains(building1));
        assertTrue(board.getUpperRow().contains(card1));
    }

    @Test
    @DisplayName("removeCardFromUpperRow method should throw IllegalStateException when remove() fails due to a card not found")
    void remove3() {
        // Setup
        Card nonExistentCard = mock(Card.class);

        // Simulate & Assert
        assertThrows(IllegalStateException.class, () -> board.removeCardFromUpperRow(nonExistentCard),
                "Should throw IllegalStateException if the card is not in the upper row or upper buildings");
    }

    @Test
    @DisplayName("Standard removeCardFromLowerRow method behaviour when called on a existent non-building Card")
    void remove4() {
        // Setup
        Card card1 = mock(Card.class);
        BuildingCard building1 = mock(BuildingCard.class);
        Board customBoard = new Board(new ArrayList<>(), List.of(card1), new ArrayList<>(), List.of(building1),
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        // Simulate
        customBoard.removeCardFromLowerRow(card1);

        // Assert
        assertEquals(1, customBoard.getLowerRow().size());
        assertTrue(customBoard.getLowerRow().contains(building1));
        assertFalse(customBoard.getLowerRow().contains(card1));
    }

    @Test
    @DisplayName("Standard removeCardFromLowerRow method behaviour when called on a existent building Card")
    void remove5() {
        // Setup
        Card card1 = mock(Card.class);
        BuildingCard building1 = mock(BuildingCard.class);
        Board customBoard = new Board(new ArrayList<>(), List.of(card1), new ArrayList<>(), List.of(building1),
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        // Simulate
        customBoard.removeCardFromLowerRow(building1);

        // Assert
        assertEquals(1, customBoard.getLowerRow().size());
        assertFalse(customBoard.getLowerRow().contains(building1));
        assertTrue(customBoard.getLowerRow().contains(card1));
    }

    @Test
    @DisplayName("removeCardFromLowerRow method should throw IllegalStateException when remove() fails due to a card not found")
    void remove6() {
        // Setup
        Card nonExistentCard = mock(Card.class);

        // Simulate & Assert
        assertThrows(IllegalStateException.class, () -> board.removeCardFromLowerRow(nonExistentCard),
                "Should throw IllegalStateException if the card is not in the upper row or upper buildings");
    }


    @Test
    @DisplayName("Standard getUpperRowCharacters and getUpperPickable methods behaviour")
    void filter1() {
        // Arrange
        CharacterCard characterCard = mock(CharacterCard.class);
        EventCard eventCard = mock(EventCard.class);
        BuildingCard buildingCard = mock(BuildingCard.class);

        when(characterCard.isPickable()).thenReturn(true);
        when(eventCard.isPickable()).thenReturn(false);
        when(buildingCard.isPickable()).thenReturn(true);

        board.addCardToUpperRow(characterCard);
        board.addCardToUpperRow(eventCard);
        board.addBuildingToUpperRow(buildingCard);

        // Act
        List<Card> characters = board.getUpperRowCharacters();
        List<Card> pickable = board.getUpperPickable();

        // Assert
        assertEquals(1, characters.size());
        assertEquals(2, pickable.size());
        assertTrue(characters.contains(characterCard));
        assertFalse(characters.contains(eventCard));
        assertFalse(characters.contains(buildingCard));
        assertTrue(pickable.contains(characterCard));
        assertFalse(pickable.contains(eventCard));
        assertTrue(pickable.contains(buildingCard));
    }

    @Test
    @DisplayName("Standard getLowerRowCharacters and getLowerPickable methods behaviour")
    void filter2() {
        // Arrange
        CharacterCard characterCard = mock(CharacterCard.class);
        EventCard eventCard = mock(EventCard.class);
        BuildingCard buildingCard = mock(BuildingCard.class);

        when(characterCard.isPickable()).thenReturn(true);
        when(eventCard.isPickable()).thenReturn(false);
        when(buildingCard.isPickable()).thenReturn(true);

        Board customBoard = new Board(new ArrayList<>(), List.of(characterCard,eventCard), new ArrayList<>(), List.of(buildingCard),
                new TurnOrderTile(numPlayers), new OfferTrack(numPlayers));

        // Act
        List<Card> characters = customBoard.getLowerRowCharacters();
        List<Card> pickable = customBoard.getLowerPickable();

        // Assert
        assertEquals(1, characters.size());
        assertEquals(2, pickable.size());
        assertTrue(characters.contains(characterCard));
        assertFalse(characters.contains(eventCard));
        assertFalse(characters.contains(buildingCard));
        assertTrue(pickable.contains(characterCard));
        assertFalse(pickable.contains(eventCard));
        assertTrue(pickable.contains(buildingCard));
    }
}