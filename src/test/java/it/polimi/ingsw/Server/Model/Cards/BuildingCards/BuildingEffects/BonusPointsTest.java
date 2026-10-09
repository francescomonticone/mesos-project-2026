package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.TotemColour;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class BonusPointsTest {

    @Test
    void onEndOfGame() {
        BuildingCard test = new BuildingCard(2, Era.I, "BL_01", 5, 0, new BonusPoints(25));
        Player player = new Player("Anna", new Tribe(List.of(test), Collections.emptyList()), 0, 0, TotemColour.WHITE, true);

        player.computeAndApplyFinalPrestige();

        assertEquals(25, player.getPrestigePoint());
    }
}