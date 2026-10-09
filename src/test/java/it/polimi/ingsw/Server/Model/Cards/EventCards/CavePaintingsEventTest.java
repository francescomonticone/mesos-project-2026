package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CavePaintingsEventTest {

    private Artist artist = new Artist(0, Era.I, "CH_ART_01", 1, "ARTIST");
    private BuildingCard cavePaintingBonus = new BuildingCard(1, Era.I, "BUI_01", 3, 2, new CavePaintingBonus(1));

    private Player player1With2ArtistWithBuilding;
    private Tribe tribe1;
    private List<CharacterCard> characterCardList1;

    private Player player2With5ArtistWithoutBuilding;
    private Tribe tribe2;
    private List<CharacterCard> characterCardList2;

    private List<BuildingCard> buildingCardList;
    private List<BuildingCard> emptyBuildingCardList;

    private List<Player> players;
    private List<Player> emptyPlayers;

    private Map<Player, Integer> foodDeltas;
    private Map<Player, Integer> prestigeDeltas;

    @BeforeEach
    void setUp() {
        characterCardList1 = new ArrayList<>(Arrays.asList(artist, artist));
        characterCardList2 = new ArrayList<>(Arrays.asList(artist, artist, artist, artist, artist));

        buildingCardList = new ArrayList<>(Arrays.asList(cavePaintingBonus));
        emptyBuildingCardList = new ArrayList<>();

        tribe1 = new Tribe(buildingCardList, characterCardList1);
        tribe2 = new Tribe(emptyBuildingCardList, characterCardList2);

        player1With2ArtistWithBuilding = new Player("patrick", tribe1, 10, 5, TotemColour.BLACK, true);
        player2With5ArtistWithoutBuilding = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1With2ArtistWithBuilding, player2With5ArtistWithoutBuilding);
        emptyPlayers = new ArrayList<>();

        foodDeltas = new HashMap<>();
        prestigeDeltas = new HashMap<>();

        foodDeltas.put(player1With2ArtistWithBuilding, 2);
        foodDeltas.put(player2With5ArtistWithoutBuilding, 0);
    }

    @Test
    @DisplayName("player1 with 2 artists loses prestigePoints, gains food thanks to the building" +
                "player2 gains prestige points")
    void calculateTest1() {
        prestigeDeltas.put(player1With2ArtistWithBuilding, -3);
        prestigeDeltas.put(player2With5ArtistWithoutBuilding, 5);

        CavePaintingsEvent cavePaintingsEvent = new CavePaintingsEvent("EV_CAV_01", Era.I, 2, false,
                2, 3, 3, 1);

        EventResult eventResult = cavePaintingsEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("player1 only gains food, nothing happens to his prestige points" +
                "player2 gains prestige points")
    void calculateTest2() {
        prestigeDeltas.put(player1With2ArtistWithBuilding, 0);
        prestigeDeltas.put(player2With5ArtistWithoutBuilding, 15);

        CavePaintingsEvent cavePaintingsEvent = new CavePaintingsEvent("EV_CAV_01", Era.I, 2, false,
                1, 3, 5, 3);

        EventResult eventResult = cavePaintingsEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("null player list")
    void calculateTest3() {
        CavePaintingsEvent cavePaintingsEvent = new CavePaintingsEvent("EV_CAV_01", Era.I, 2, false,
                1, 3, 5, 3);

        assertThrows(IllegalArgumentException.class,
                () -> cavePaintingsEvent.calculate(null)
                );
    }

    @Test
    @DisplayName("empty player list")
    void calculateTest4() {
        CavePaintingsEvent cavePaintingsEvent = new CavePaintingsEvent("EV_CAV_01", Era.I, 2, false,
                1, 3, 5, 3);

        assertThrows(IllegalArgumentException.class,
                () -> cavePaintingsEvent.calculate(emptyPlayers)
        );
    }
}