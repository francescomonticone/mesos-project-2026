package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Gatherer;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GathererTest {

    Gatherer gatherer;

    @BeforeEach
    void setUp() {
        gatherer = new Gatherer(2, Era.I, "CH_GAT_01", 3, "GATHERER");
    }

    @Test
    void getFoodDiscount() {
        assertEquals(3, gatherer.getFoodDiscount());
    }

    @Test
    void getType() {
        assertEquals("GATHERER", gatherer.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        gatherer.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(gatherer);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}