package it.polimi.ingsw.Server.Model.Cards.BuildingCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Match.Era;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuildingCardTest {

    private BuildingCard building;

    @BeforeEach
    void setup(){
        building = new BuildingCard(2, Era.I, "BL_01", 3, 6, new BonusPoints(25));
    }

    @Test
    void getFoodCost() {
        assertEquals(3, building.getFoodCost());
    }

    @Test
    void getFinalPrestigePoints() {
        assertEquals(6, building.getFinalPrestigePoints());
    }

    @Test
    void getEffect() {
        assertInstanceOf(BonusPoints.class, building.getEffect());
    }

    @Test
    void isPickable() {
        assertTrue(building.isPickable());
    }
}