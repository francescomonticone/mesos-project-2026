package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraShamanicStar;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Shaman;
import it.polimi.ingsw.Server.Model.Cards.EventCards.ShamanicRitualEvent;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.TotemColour;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtraShamanicStarTest {
    private ShamanicRitualEvent event = new ShamanicRitualEvent("EV1", Era.I,  2, false, 10, 5);

    private Player player1;
    private Player player2;
    private Player player3;

    private BuildingCard test = new BuildingCard(2, Era.I, "BL01", 0, 0, new ExtraShamanicStar(3));

    private CharacterCard shaman =  new Shaman(2, Era.I, "CH", 2, "SHAMAN");
    private CharacterCard character = mock(CharacterCard.class);

    @Test
    @DisplayName("Normal behaviour of ExtraShamanicStar effect")
    void onShamanicEvent_normal(){
        when(character.getShamanStars()).thenReturn(0);
        player1 = new Player("Anna", new Tribe(Collections.singletonList(test), Collections.singletonList(character)),
                0, 0, TotemColour.WHITE, true);
        player2 = new Player("Bruno", new Tribe(Collections.emptyList(), Collections.singletonList(shaman)),
                0, 0, TotemColour.WHITE, true);
        player3 = new Player("Carlo", new Tribe(Collections.emptyList(), Collections.emptyList()),
                0, 0, TotemColour.WHITE, true);

        EventResult result = event.calculate(Arrays.asList(player1, player2, player3));

        Map<Player, Integer> foodDeltas = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();

        foodDeltas.put(player1, 0);
        foodDeltas.put(player2, 0);
        foodDeltas.put(player3, 0);
        prestigeDeltas.put(player1, 10);
        prestigeDeltas.put(player2, 0);
        prestigeDeltas.put(player3, -5);

        assertEquals(foodDeltas, result.getFoodDeltas());
        assertEquals(prestigeDeltas, result.getPrestigePointsDeltas());
    }

}