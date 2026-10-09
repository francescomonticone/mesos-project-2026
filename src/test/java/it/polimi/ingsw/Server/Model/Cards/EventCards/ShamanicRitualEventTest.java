package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.DoubleShamanicPoints;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraShamanicStar;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ShamanicShield;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Shaman;
import it.polimi.ingsw.Server.Model.Cards.EventCards.ShamanicRitualEvent;
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

class ShamanicRitualEventTest {
    private Shaman shaman = new Shaman(2, Era.I, "CH_SHA_01", 2, "SHAMAN");

    private BuildingCard extraShamanicStar = new BuildingCard(2, Era.I, "BL_01", 3, 10, new ExtraShamanicStar(3));
    private BuildingCard shamanicShield = new BuildingCard(2, Era.I, "BL_01", 3, 10, new ShamanicShield());
    private BuildingCard doubleShamanicPoints = new BuildingCard(2, Era.I, "BL_01", 3, 10, new DoubleShamanicPoints());

    private Player player1;
    private Tribe tribe1;
    private List<CharacterCard> characterCardList1;
    private List<BuildingCard> buildingCardList1;

    private Player player2;
    private Tribe tribe2;
    private List<CharacterCard> characterCardList2;
    private List<BuildingCard> buildingCardList2;

    private List<Player> players;
    private List<Player> emptyPlayers;

    private Map<Player, Integer> foodDeltas;
    private Map<Player, Integer> prestigeDeltas;

    @BeforeEach
    void setUp() {
        emptyPlayers = new ArrayList<>();

        foodDeltas = new HashMap<>();
        prestigeDeltas = new HashMap<>();
    }

    @Test
    @DisplayName("player 1: 4+3 shamanic points with extraShamanicStar building" +
                "player 2: 4 shamanic points with shamanicShield and doubleShamanicPoints")
    void calculateTest1() {
        characterCardList1 = new ArrayList<>(Arrays.asList(shaman, shaman));
        characterCardList2 = new ArrayList<>(Arrays.asList(shaman, shaman));

        buildingCardList1 = new ArrayList<>(Arrays.asList(extraShamanicStar));
        buildingCardList2 = new ArrayList<>(Arrays.asList(shamanicShield, doubleShamanicPoints));

        tribe1 = new Tribe(buildingCardList1, characterCardList1);
        tribe2 = new Tribe(buildingCardList2, characterCardList2);

        player1 = new Player("patrick", tribe1, 10, 5, TotemColour.BLACK, true);
        player2 = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1, player2);

        prestigeDeltas.put(player1, 2);
        prestigeDeltas.put(player2, 0);
        foodDeltas.put(player1, 0);
        foodDeltas.put(player2, 0);

        ShamanicRitualEvent shamanicRitualEvent = new ShamanicRitualEvent("EV_SHA_01", Era.I, 2, false,
                2, 1);

        EventResult eventResult = shamanicRitualEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("player 1: 4+3 shamanic points with extraShamanicStar building" +
            "player 2: 8 shamanic points with shamanicShield and doubleShamanicPoints")
    void calculateTest2() {
        characterCardList1 = new ArrayList<>(Arrays.asList(shaman, shaman));
        characterCardList2 = new ArrayList<>(Arrays.asList(shaman, shaman, shaman, shaman));

        buildingCardList1 = new ArrayList<>(Arrays.asList(extraShamanicStar));
        buildingCardList2 = new ArrayList<>(Arrays.asList(shamanicShield, doubleShamanicPoints));

        tribe1 = new Tribe(buildingCardList1, characterCardList1);
        tribe2 = new Tribe(buildingCardList2, characterCardList2);

        player1 = new Player("patrick", tribe1, 10, 5, TotemColour.BLACK, true);
        player2 = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1, player2);

        prestigeDeltas.put(player1, -1);
        prestigeDeltas.put(player2, 4);
        foodDeltas.put(player1, 0);
        foodDeltas.put(player2, 0);

        ShamanicRitualEvent shamanicRitualEvent = new ShamanicRitualEvent("EV_SHA_01", Era.I, 2, false,
                2, 1);

        EventResult eventResult = shamanicRitualEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("player 1: 4 shamanic points " +
            "player 2: 4 shamanic points with shamanicShield and doubleShamanicPoints")
    void calculateTest3() {
        characterCardList1 = new ArrayList<>(Arrays.asList(shaman, shaman));
        characterCardList2 = new ArrayList<>(Arrays.asList(shaman, shaman));

        buildingCardList1 = new ArrayList<>();
        buildingCardList2 = new ArrayList<>(Arrays.asList(shamanicShield, doubleShamanicPoints));

        tribe1 = new Tribe(buildingCardList1, characterCardList1);
        tribe2 = new Tribe(buildingCardList2, characterCardList2);

        player1 = new Player("patrick", tribe1, 10, 5, TotemColour.BLACK, true);
        player2 = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1, player2);

        prestigeDeltas.put(player1, 1);
        prestigeDeltas.put(player2, 2);
        foodDeltas.put(player1, 0);
        foodDeltas.put(player2, 0);

        ShamanicRitualEvent shamanicRitualEvent = new ShamanicRitualEvent("EV_SHA_01", Era.I, 2, false,
                2, 1);

        EventResult eventResult = shamanicRitualEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("null players list")
    void calculateTest4() {
        ShamanicRitualEvent shamanicRitualEvent = new ShamanicRitualEvent("EV_SHA_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> shamanicRitualEvent.calculate(null)
        );
    }

    @Test
    @DisplayName("empty players list")
    void calculateTest5() {
        ShamanicRitualEvent shamanicRitualEvent = new ShamanicRitualEvent("EV_SHA_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> shamanicRitualEvent.calculate(emptyPlayers)
        );
    }
}