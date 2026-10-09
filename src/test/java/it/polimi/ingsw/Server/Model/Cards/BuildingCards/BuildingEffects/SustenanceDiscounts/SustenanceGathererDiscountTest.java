package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Gatherer;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SustenanceGathererDiscountTest {

    private Player player1;
    private Player player2;
    private Player player3;


    private CharacterCard gatherer = new Gatherer(2, Era.I, "Ch", 3, "GATHERER");
    private CharacterCard character = mock(CharacterCard.class);

    private BuildingCard gathererDiscount = new BuildingCard(2, Era.I, "B1", 0, 0, new SustenanceGatherersDiscount(1));
    private BuildingCard building = new BuildingCard(2, Era.I, "B1", 0, 0, new BonusPoints(1));

    @BeforeEach
    void setup(){
        player1 = new Player("Anna", new Tribe(Collections.singletonList(gathererDiscount), Arrays.asList(gatherer, character, character, character)),
                10, 0, null, true);
        player2 = new Player("Bruno", new Tribe(Collections.singletonList(building), Arrays.asList(gatherer, character, character, character, character)),
                2, 0, null, true);
        player3 = new Player("Carlo", new Tribe(Collections.singletonList(building), Arrays.asList(character, character)),
                10, 0, null, true);

        when(character.getFoodDiscount()).thenReturn(0);
    }

    @Test
    @DisplayName("Check normal behaviour")
    void DiscountTest(){
        // setup
        SustenanceEvent event = new SustenanceEvent("E1", Era.I, 2, false, 2, 1);


        Map<Player, Integer> foodDeltas = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();

        // simulate event
        EventResult result = event.calculate(Arrays.asList(player1, player2, player3));

        // expected result
        foodDeltas.put(player1, 0);
        foodDeltas.put(player2, -2);
        foodDeltas.put(player3, -2);

        prestigeDeltas.put(player1, 0);
        prestigeDeltas.put(player2, 0);
        prestigeDeltas.put(player3, 0);

        // check if event result correspond to expected result
        assertEquals(foodDeltas, result.getFoodDeltas());
        assertEquals(prestigeDeltas, result.getPrestigePointsDeltas());
    }

}