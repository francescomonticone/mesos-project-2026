package it.polimi.ingsw.Server.Model.Match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class EventResultTest {

    private Player player1;
    private Player player2;

    @BeforeEach
    void setup() {
        player1 = new Player("Anna");
        player2 = new Player("Bruno");
    }

    @Test
    @DisplayName("Default constructor test")
    void constructor() {
        // Setup
        Map<Player, Integer> prestigeDeltas = Map.of(player1, 5, player2, -2);
        Map<Player, Integer> foodDeltas = Map.of(player1, 0, player2, 3);

        // Simulate
        EventResult result = new EventResult(prestigeDeltas, foodDeltas);

        // Assert
        assertAll(
                () -> assertNotNull(result.getPrestigePointsDeltas()),
                () -> assertNotNull(result.getFoodDeltas()),
                () -> assertEquals(5, result.getPrestigePointsDeltas().get(player1)),
                () -> assertEquals(-2, result.getPrestigePointsDeltas().get(player2)),
                () -> assertEquals(0, result.getFoodDeltas().get(player1)),
                () -> assertEquals(3, result.getFoodDeltas().get(player2))
        );
    }

    @Test
    @DisplayName("If key set don't match constructor should throw an IllegalArgumentException")
    void constructor2() {
        // Setup
        Map<Player, Integer> prestigeDeltas = Map.of(player1, 5);
        Map<Player, Integer> foodDeltas = Map.of(player2, 3); // Mismatched player keys

        // Simulate & Assert
        assertThrows(IllegalArgumentException.class, () -> new EventResult(prestigeDeltas, foodDeltas));
    }

    @Test
    @DisplayName("Check if constructor creates a defensive copy")
    void constructor3() {
        // Setup
        Map<Player, Integer> prestigeDeltas = new HashMap<>();
        prestigeDeltas.put(player1, 10);
        Map<Player, Integer> foodDeltas = new HashMap<>();
        foodDeltas.put(player1, 4);

        EventResult result = new EventResult(prestigeDeltas, foodDeltas);

        // Simulate
        prestigeDeltas.put(player1, 999);
        foodDeltas.put(player1, 888);

        // Assert: internal state must be immune to external change
        assertEquals(10, result.getPrestigePointsDeltas().get(player1));
        assertEquals(4, result.getFoodDeltas().get(player1));
    }

    @Test
    @DisplayName("Getters should return immutable Maps")
    void getters() {
        // Setup
        Map<Player, Integer> prestigeDeltas = Map.of(player1, 1);
        Map<Player, Integer> foodDeltas = Map.of(player1, 1);
        EventResult result = new EventResult(prestigeDeltas, foodDeltas);

        // Simulate & Assert
        Map<Player, Integer> returnedPrestige = result.getPrestigePointsDeltas();
        assertThrows(UnsupportedOperationException.class, () -> returnedPrestige.put(player2, 10),
                "Returned prestige points map must be unmodifiable");

        Map<Player, Integer> returnedFood = result.getFoodDeltas();
        assertThrows(UnsupportedOperationException.class, () -> returnedFood.put(player2, 5),
                "Returned food map must be unmodifiable");
    }
}