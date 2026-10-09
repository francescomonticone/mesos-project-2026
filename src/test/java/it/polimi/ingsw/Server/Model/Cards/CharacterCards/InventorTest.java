package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.InventionIcon;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Inventor;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventorTest {

    Inventor inventor;

    @BeforeEach
    void setUp() {
        inventor = new Inventor(2, Era.I, "CH_INV_01", InventionIcon.BOWL, "INVENTOR");
    }

    @Test
    void getInvention() {
        assertEquals(InventionIcon.BOWL, inventor.getInvention());
    }

    @Test
    void getType() {
        assertEquals("INVENTOR", inventor.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        inventor.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(inventor);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}