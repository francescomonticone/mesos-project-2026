package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.BonusPoints;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.CavePaintingBonus;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Artist;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.CavePaintingsEvent;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.TotemColour;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CavePaintingBonusTest {
    private BuildingCard test = new BuildingCard(2, Era.I, "B1", 0, 0, new CavePaintingBonus(1));
    private BuildingCard building = new BuildingCard(2,Era.I, "B1", 0, 0, new BonusPoints(1));

    private CharacterCard artist;
    private CharacterCard genericCharacter;

    private CavePaintingsEvent event;

    private Map<Player, Integer> foodDeltas;
    private Map<Player, Integer> prestigeDeltas;

    @BeforeEach
    void setup(){
        artist = mock(Artist.class);
        genericCharacter = mock(CharacterCard.class);
        foodDeltas = new HashMap<>();
        prestigeDeltas = new HashMap<>();

    }

    @Test
    void onCavePaintingEvent_normal() {
        when(artist.getArtistBonus()).thenReturn(1);
        when(genericCharacter.getArtistBonus()).thenReturn(0);

        Player player1 = new Player("Anna", new Tribe(Collections.singletonList(test), Arrays.asList(artist, artist, artist, genericCharacter, genericCharacter)),
                0, 0, TotemColour.WHITE, true);
        Player player2 = new Player("Bruno", new Tribe(Collections.emptyList(), Arrays.asList(artist, genericCharacter, genericCharacter)),
                0, 0, TotemColour.WHITE, true);
        Player player3 = new Player("Carlo", new Tribe(Arrays.asList(building, building), Arrays.asList(genericCharacter, genericCharacter)),
                0, 0, TotemColour.WHITE, true);

        event = new CavePaintingsEvent("EV_CAV_01", Era.I, 2, false, 0, 2, 2, 2);

        EventResult result = event.calculate(Arrays.asList(player1, player2, player3));

        //expected values based on lossThreshold and gainThreshold parameters
        foodDeltas.put(player1, 3);
        foodDeltas.put(player2, 0);
        foodDeltas.put(player3, 0);

        prestigeDeltas.put(player1, 6);
        prestigeDeltas.put(player2, 0);
        prestigeDeltas.put(player3, -2);

        assertAll(
            () -> assertEquals(foodDeltas, result.getFoodDeltas()),
            () -> assertEquals(prestigeDeltas, result.getPrestigePointsDeltas())
        );
    }
}