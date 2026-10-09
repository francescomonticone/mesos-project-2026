package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodGainTest {

    @Test
    @DisplayName("Standard applyAutomaticEffect method behaviour")
    void effect(){
        // Setup
        Player p = new Player("Anna");
        int food = 3;
        FoodGain action = new FoodGain(food);

        // Simulate
        action.applyAutomaticEffect(p);

        // Assert
        assertEquals(food, p.getFoodToken());
    }
}