package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Artist;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistTest {

    private Artist artist;

    @BeforeEach
    void setup() {
        artist = new Artist(0, Era.I, "CH_ART_01", 1, "ARTIST");
    }

    @Test
    void getArtistBonus() {
        assertEquals(1, artist.getArtistBonus());
    }

    @Test
    void getType() {
        assertEquals("ARTIST", artist.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        artist.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(artist);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}