package it.polimi.ingsw.Server.Model.Match;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlayerTest {

    private String nick;
    private Player player;

    @BeforeEach
    void setup(){
        nick = "Anna";
        player = new Player(nick);
    }

    @Test
    @DisplayName("Default constructor test")
    void constructor(){
        assertEquals(nick, player.getNickname());
        assertNotNull(player.getTribe());
        assertEquals(0, player.getFoodToken());
        assertEquals(0, player.getPrestigePoint());
        assertNull(player.getTotem());
        assertTrue(player.isConnected());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("If nickname is not valid constructor should throw IllegalArgumentException")
    void constructor2(String invalidNick){
        // Simulate & Assert
        assertThrows(IllegalArgumentException.class, () -> new Player(invalidNick));
    }

    @Test
    @DisplayName("Standard removeFood method behaviour")
    void remove(){
        // Setup
        player.addFood(1);
        assertEquals(1, player.getFoodToken());

        // Simulate
        player.removeFood(1);

        // Assert
        assertEquals(0, player.getFoodToken());
    }

    @Test
    @DisplayName("If food amount to remove is negative or greater than current amount, removeFood method should throw an exception")
    void remove2(){
        // Simulate & Assert
        assertThrows(IllegalArgumentException.class, () -> player.removeFood(-1));
        assertThrows(IllegalStateException.class, () -> player.removeFood(1));
    }

    @Test
    @DisplayName("Standard removePrestigePoint method behaviour")
    void remove3(){
        // Setup
        player.addPrestigePoint(1);
        assertEquals(1, player.getPrestigePoint());

        // Simulate
        player.removePrestigePoint(1);

        // Assert
        assertEquals(0, player.getPrestigePoint());
    }

    @Test
    @DisplayName("If points to remove is negative, removePrestigePoint method should throw an exception")
    void remove4(){
        // Simulate & Assert
        assertThrows(IllegalArgumentException.class, () -> player.removeFood(-1));
    }

    @Test
    @DisplayName("Standard applyLastSpacePenalty method behaviour")
    void penalty(){
        // Setup
        player.addFood(1);

        // Simulate & Assert
        player.applyLastSpacePenalty();
        assertEquals(0, player.getFoodToken());

        player.applyLastSpacePenalty();
        assertEquals(0, player.getFoodToken());
        assertEquals(-2, player.getPrestigePoint());
    }

    @Test
    @DisplayName("Standard computeAndApplyFinalPrestige method behaviour")
    void compute(){
        // Setup
        Tribe t = mock(Tribe.class);
        Player p = new Player("Anna", t, 0,0, null, true);

        when(t.getFinalBuildingPrestigePoints()).thenReturn(1);
        when(t.getFinalBuilderPoints()).thenReturn(1);
        when(t.getTotalInventorCount()).thenReturn(1);
        when(t.getDistinctInventionsCount()).thenReturn(1);
        when(t.getFinalCoupleArtistBonus()).thenReturn(1);
        when(t.calculateEndGameBuildingEffects()).thenReturn(1);

        // Simulate
        p.computeAndApplyFinalPrestige();

        // Assert
        assertEquals(5, p.getPrestigePoint());
        assertThrows(IllegalStateException.class, p::computeAndApplyFinalPrestige);
    }

    @Test
    @DisplayName("Standard addCharacterToTribe and addBuildingToTribe methods behaviour")
    void add(){
        // Setup
        CharacterCard characterCard = mock(CharacterCard.class);
        BuildingCard buildingCard = mock(BuildingCard.class);

        assertEquals(0, player.getTribe().getBuildingCardList().size());
        assertEquals(0, player.getTribe().getCharacterCardList().size());

        // Simulate
        player.addCharacterToTribe(characterCard);
        player.addBuildingToTribe(buildingCard);

        // Assert
        List<CharacterCard> characterCardList = player.getTribe().getCharacterCardList();
        List<BuildingCard> buildingCardList = player.getTribe().getBuildingCardList();

        assertEquals(1, characterCardList.size());
        assertEquals(1, buildingCardList.size());
        assertTrue(characterCardList.contains(characterCard));
        assertFalse(characterCardList.contains(buildingCard));
        assertFalse(buildingCardList.contains(characterCard));
        assertTrue(buildingCardList.contains(buildingCard));

    }
}