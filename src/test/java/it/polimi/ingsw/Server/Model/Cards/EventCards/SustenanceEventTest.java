package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceArtistDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceGatherersDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceInventorsDiscount;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Artist;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Gatherer;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.InventionIcon;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Inventor;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
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

class SustenanceEventTest {
    private Artist artist = new Artist(0, Era.I, "CH_ART_01", 1, "ARTIST");
    private Gatherer gatherer = new Gatherer(2, Era.I, "CH_GAT_01", 3, "GATHERER");
    private Inventor inventor = new Inventor(2, Era.I, "CH_INV_01", InventionIcon.BOWL, "INVENTOR");

    private BuildingCard sustenanceArtistDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceArtistDiscount(1));
    private BuildingCard sustenanceGatherersDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceGatherersDiscount(1));
    private BuildingCard sustenanceInventorsDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceInventorsDiscount(1));

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
    @DisplayName("player1: insufficient foodToken, pays prestige points. " +
            "player2: has too much food discount")
    void calculateTest1() {
        //player1 has 1 artist, 4 inventor and 1 gatherer and has sustenanceArtistDiscount building
        characterCardList1 = new ArrayList<>(Arrays.asList(artist, inventor, inventor, inventor, inventor, gatherer));
        buildingCardList1 = new ArrayList<>(Collections.singletonList(sustenanceArtistDiscount));
        tribe1 = new Tribe(buildingCardList1, characterCardList1);
        player1 = new Player("patrick", tribe1, 1, 5, TotemColour.BLACK, true);

        //player2 has 2 gatherers, 1 artist, sustenanceGatherersDiscount and sustenanceInventorsDiscount buildings
        characterCardList2 = new ArrayList<>(Arrays.asList(gatherer, gatherer, artist));
        buildingCardList2 = new ArrayList<>(Arrays.asList(sustenanceGatherersDiscount, sustenanceInventorsDiscount));
        tribe2 = new Tribe(buildingCardList2, characterCardList2);
        player2 = new Player("kevin", tribe2, 5, 10, TotemColour.ORANGE, true);

        players = Arrays.asList(player1, player2);

        //player1 has 6 characters, 1 gatherer = 3 food discount and 1 artist = 1 food discount
        //totalCost = 6 - 3 - 1 = 2
        //player1 has 1 foodToken so he feeds only 1 character. he pays prestige Points for the 1 starving character
        //foodDeltas = -numberOfFedCharacters = -1
        //prestigeDeltas = -(starvingCharacters * prestigePointsCost) = -(1*2)
        prestigeDeltas.put(player1, -2);
        foodDeltas.put(player1, -1);

        //player2 has 3 characters, 2 gatherers = 2*3 + 1*2 food discount
        //totalCost = 3 - (2*3 + 1*2) = -5
        //all characters are already fed from food discount
        prestigeDeltas.put(player2, 0);
        foodDeltas.put(player2, 0);

        SustenanceEvent sustenanceEvent = new SustenanceEvent("EV_SUS_01", Era.I, 2, false,
                2, 1);

        EventResult eventResult = sustenanceEvent.calculate(players);

        assertAll(
                () -> assertEquals(prestigeDeltas, eventResult.getPrestigePointsDeltas()),
                () -> assertEquals(foodDeltas, eventResult.getFoodDeltas())
        );
    }

    @Test
    @DisplayName("null players list")
    void calculateTest2() {
        SustenanceEvent sustenanceEvent = new SustenanceEvent("EV_SUS_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> sustenanceEvent.calculate(null)
        );
    }

    @Test
    @DisplayName("empty players list")
    void calculateTest3() {
        SustenanceEvent sustenanceEvent = new SustenanceEvent("EV_SUS_01", Era.I, 2, false,
                2, 1);

        assertThrows(IllegalArgumentException.class,
                () -> sustenanceEvent.calculate(emptyPlayers)
        );
    }
}