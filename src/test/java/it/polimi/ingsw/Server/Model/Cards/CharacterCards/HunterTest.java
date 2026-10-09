package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HunterTest {

    Hunter hunter;

    @BeforeEach
    void setUp(){
        hunter = new Hunter(2, Era.I, "CH_HUN_01", true, 1, "HUNTER");
    }

    @Test
    void getFoodBonus() {
        assertEquals(1, hunter.getFoodBonus());
    }

    @Test
    void getType() {
        assertEquals("HUNTER", hunter.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        hunter.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(hunter);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}