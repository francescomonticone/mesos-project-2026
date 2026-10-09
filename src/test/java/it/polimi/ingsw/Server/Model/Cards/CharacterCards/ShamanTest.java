package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Shaman;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShamanTest {

    private Shaman shaman;

    @BeforeEach
    void setUp() {
        shaman = new Shaman(2, Era.I, "CH_SHA_01", 2, "SHAMAN");
    }

    @Test
    void getShamanStars() {
        assertEquals(2, shaman.getShamanStars());
    }

    @Test
    void getType() {
        assertEquals("SHAMAN", shaman.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        shaman.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(shaman);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}