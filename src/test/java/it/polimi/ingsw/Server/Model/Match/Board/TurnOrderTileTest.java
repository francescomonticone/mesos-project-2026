package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TurnOrderTileTest {

    private TurnOrderTile turnOrderTile;
    private final int numPlayers = 5;

    @BeforeEach
    void setup(){
        turnOrderTile = new TurnOrderTile(numPlayers);
    }

    @Test
    @DisplayName("Default constructor test")
    void constructor(){
        assertNotNull(turnOrderTile.getPlayerOrderTopBottom());
        assertTrue(turnOrderTile.getPlayerOrderTopBottom().isEmpty());
    }

    @Test
    @DisplayName("Checks if food bonuses are correctly initialized for every possible TurnOrderTile")
    void init_test(){
        TurnOrderTile test = new TurnOrderTile(2);
        assertEquals(1,test.getFoodBonusFromPosition(0));
        assertEquals(0,test.getFoodBonusFromPosition(1));

        test = new TurnOrderTile(3);
        assertEquals(2,test.getFoodBonusFromPosition(0));
        assertEquals(0,test.getFoodBonusFromPosition(1));
        assertEquals(0,test.getFoodBonusFromPosition(2));

        test = new TurnOrderTile(4);
        assertEquals(2,test.getFoodBonusFromPosition(0));
        assertEquals(1,test.getFoodBonusFromPosition(1));
        assertEquals(0,test.getFoodBonusFromPosition(2));
        assertEquals(0,test.getFoodBonusFromPosition(3));

        test = new TurnOrderTile(5);
        assertEquals(3,test.getFoodBonusFromPosition(0));
        assertEquals(1,test.getFoodBonusFromPosition(1));
        assertEquals(0,test.getFoodBonusFromPosition(2));
        assertEquals(0,test.getFoodBonusFromPosition(3));
        assertEquals(0,test.getFoodBonusFromPosition(4));

    }

    @Test
    @DisplayName("Standard placeTotemOnTurnOrderTile method behaviour")
    void place(){
        // Setup
        Player p1 = new Player("Anna");
        Player p2 = new Player("Bruno");

        // need to set and reset player order at least one time to have a size different from 0
        turnOrderTile.setPlayerOrderTopBottom(List.of(p1,p2));
        turnOrderTile.reset();
        assertFalse(turnOrderTile.getPlayerOrderTopBottom().isEmpty());

        // Simulate
        int firstIndex = turnOrderTile.placeTotemOnTurnOrderTile(p2);
        int secondIndex = turnOrderTile.placeTotemOnTurnOrderTile(p1);

        // Assert
        assertFalse(turnOrderTile.getPlayerOrderTopBottom().isEmpty());
        assertNotNull(turnOrderTile.getPlayerOrderTopBottom().getFirst());
        assertNotNull(turnOrderTile.getPlayerOrderTopBottom().get(1));
        assertEquals(0, firstIndex);
        assertEquals(1, secondIndex);
        assertEquals(turnOrderTile.getPlayerOrderTopBottom(), List.of(p2,p1));
    }

    @Test
    @DisplayName("If TurnOrderTile is full, placeTotemOnTurnOrderTile should throw an IllegalStateException")
    void place2(){
        // Simulate & Assert
        assertThrows(IllegalStateException.class, () -> turnOrderTile.placeTotemOnTurnOrderTile(new Player("Anna")));
    }

    @Test
    @DisplayName("Standard placeTotemOnTurnOrderTile method behaviour")
    void reset() {
        // Setup
        Player p1 = new Player("Anna");
        Player p2 = new Player("Bruno");
        turnOrderTile.setPlayerOrderTopBottom(List.of(p1, p2));
        List<Player> order = turnOrderTile.getPlayerOrderTopBottom();

        // Simulate
        turnOrderTile.reset();

        // Assert
        for (int i = 0; i < order.size(); i++) {
            assertNull(turnOrderTile.getPlayerOrderTopBottom().get(i));
        }
    }

    @Test
    @DisplayName("Standard removeFromTurnOrderTile method behaviour")
    void remove(){
        // Setup
        Player p1 = new Player("Anna");
        Player p2 = new Player("Bruno");
        turnOrderTile.setPlayerOrderTopBottom(List.of(p1,p2));
        List<Player> initialOrder = turnOrderTile.getPlayerOrderTopBottom();

        // Simulate
        turnOrderTile.removeFromTurnOrderTile(0);

        // Assert
        assertEquals(initialOrder.size(), turnOrderTile.getPlayerOrderTopBottom().size());
        assertFalse(turnOrderTile.getPlayerOrderTopBottom().contains(p1));
        assertNull(turnOrderTile.getPlayerOrderTopBottom().getFirst());
        assertTrue(turnOrderTile.getPlayerOrderTopBottom().contains(p2));
    }

}