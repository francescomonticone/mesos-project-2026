package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.HuntEventBonus;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Hunter;
import it.polimi.ingsw.Server.Model.Cards.EventCards.HuntEvent;
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

class HuntEventTest {

    private Hunter hunter = new Hunter(2, Era.I, "CH_HUN_01", true, 1, "HUNTER");
    private BuildingCard huntEventBonus = new BuildingCard(2, Era.I, "BL_01", 3, 10, new HuntEventBonus(1,1));

    private Player player1;
    private Tribe tribe1;
    private List<CharacterCard> characterCardList1;

    private Player player2;
    private Tribe tribe2;
    private List<CharacterCard> characterCardList2;

    private List<BuildingCard> emptyBuildingCardList;
    private List<BuildingCard> buildingCardList;

    private List<Player> players;
    private List<Player> emptyPlayers;

    private Map<Player, Integer> foodDeltas;
    private Map<Player, Integer> prestigeDeltas;

    @BeforeEach
    void setUp() {
        //player 1 has 2 hunters and doesn't have the huntEventBonus building
        characterCardList1 = new ArrayList<>(Arrays.asList(hunter, hunter));
        emptyBuildingCardList = new ArrayList<>();
        tribe1 = new Tribe(emptyBuildingCardList, characterCardList1);
        player1 = new Player("patrick", tribe1, 10, 5, TotemColour.BLACK, true);

        //player 2 has 4 hunters and has the huntEventBonus building
        characterCardList2 = new ArrayList<>(Arrays.asList(hunter, hunter, hunter, hunter));
        buildingCardList = new ArrayList<>(Arrays.asList(huntEventBonus));
        tribe2 = new Tribe(buildingCardList, characterCardList2);
        player2 = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1, player2);
        emptyPlayers = new ArrayList<>();

        foodDeltas = new HashMap<>();
        prestigeDeltas = new HashMap<>();
    }

    @Test
    @DisplayName("player1: 2 hunter and doesn't have huntEventBonus building" +
            "player2: 4 hunters and has huntEventBonus building")
    void calculateTest1() {
        //foodDelta = huntFoodBonus * nHunter = 1*2
        //prestigeDelta = prestigePointsBonus * nHunter = 2*2
        foodDeltas.put(player1, 2);
        prestigeDeltas.put(player1, 4);

        //foodDelta = huntFoodBonus * nHunter + buildingFoodBonus * nHunter = 1*4 + 1*4
        //prestigeDelta = prestigePointsBonus * nHunter + buildingPointsBonus * nHunter= 2*4 + 1*4
        foodDeltas.put(player2, 8);
        prestigeDeltas.put(player2, 12);

        HuntEvent huntEvent = new HuntEvent("EV_HUN_01", Era.I, 2, false,
                2, 1);

        EventResult eventResult = huntEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("null players list -> throws illegalArgumentException")
    void calculateTest2() {
        HuntEvent huntEvent = new HuntEvent("EV_HUN_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> huntEvent.calculate(null)
        );
    }

    @Test
    @DisplayName("empty players list -> throws illegalArgumentException")
    void calculateTest3() {
        HuntEvent huntEvent = new HuntEvent("EV_HUN_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> huntEvent.calculate(emptyPlayers)
        );
    }
}