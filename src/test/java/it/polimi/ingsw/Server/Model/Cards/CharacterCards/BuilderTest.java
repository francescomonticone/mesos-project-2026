package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Builder;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    private Builder builder;

    @BeforeEach
    void setUp() {
        builder = new Builder(2, Era.I, "CH_BUI_01", 3, 5, "BUILDER");
    }

    @Test
    void getBuilderPoints() {
        assertEquals(5, builder.getBuilderPoints());
    }

    @Test
    void getBuildingDiscount() {
        assertEquals(3, builder.getBuildingDiscount());
    }

    @Test
    void getType() {
        assertEquals("BUILDER", builder.getType());
    }

    @Test
    void add(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        builder.addToTribe(p);

        // Assert
        List<CharacterCard> expected = List.of(builder);

        assertFalse(p.getTribe().getCharacterCardList().isEmpty());
        assertEquals(expected, p.getTribe().getCharacterCardList());
    }
}