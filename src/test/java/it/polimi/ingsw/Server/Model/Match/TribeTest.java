package it.polimi.ingsw.Server.Model.Match;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.*;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceArtistDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceGatherersDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceInventorsDiscount;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Artist;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Builder;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.CharacterCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Gatherer;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Hunter;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.InventionIcon;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Inventor;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Shaman;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class TribeTest {

    private final Artist artist = new Artist(0, Era.I, "CH_ART_01", 1, "ARTIST");
    private final Builder builder = new Builder(2, Era.I, "CH_BUI_01", 3, 5, "BUILDER");
    private final Gatherer gatherer = new Gatherer(2, Era.I, "CH_GAT_01", 3, "GATHERER");
    private final Hunter hunter = new Hunter(2, Era.I, "CH_HUN_01", true, 1, "HUNTER");
    private final Inventor inventorBowl = new Inventor(2, Era.I, "CH_INV_01", InventionIcon.BOWL, "INVENTOR");
    private final Inventor inventorArrow = new Inventor(2, Era.I, "CH_INV_02", InventionIcon.ARROW, "INVENTOR");
    private final Shaman shaman = new Shaman(2, Era.I, "CH_SHA_01", 2, "SHAMAN");

    private final BuildingCard extraShamanicStar = new BuildingCard(2, Era.I, "BL_01", 3, 10, new ExtraShamanicStar(3));
    private final BuildingCard shamanicShield = new BuildingCard(2, Era.I, "BL_01", 3, 10, new ShamanicShield());
    private final BuildingCard doubleShamanicPoints = new BuildingCard(2, Era.I, "BL_01", 3, 10, new DoubleShamanicPoints());
    private final BuildingCard extraFoodTurnEnd = new BuildingCard(2, Era.I, "BL_01", 3, 10, new ExtraFoodTurnEnd(2));
    private final BuildingCard extra1UpCardPick = new BuildingCard(2, Era.III, "BL_02", 3, 10, new ExtraCardPick(1, 0));
    private final BuildingCard extra1Up1DownCardPick = new BuildingCard(2, Era.III, "BL_02", 3, 10, new ExtraCardPick(1, 1));
    private final BuildingCard extra1DownCardPick = new BuildingCard(2, Era.III, "BL_02", 3, 10, new ExtraCardPick(0, 1));
    private final BuildingCard doubleBuilderPoints = new BuildingCard(2, Era.II, "BL_03", 3, 4, new DoubleBuilderPoints());
    private final BuildingCard sustenanceArtistDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceArtistDiscount(1));
    private final BuildingCard sustenanceGathererDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceGatherersDiscount(1));
    private final BuildingCard sustenanceInventorDiscount = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SustenanceInventorsDiscount(1));
    private final BuildingCard huntEventBonus = new BuildingCard(2, Era.I, "BL_01", 3, 10, new HuntEventBonus(1,1));
    private final BuildingCard cavePaintingBonus = new BuildingCard(2, Era.I, "BL_01", 3, 10, new CavePaintingBonus(1));
    private final BuildingCard sameInventorPairBonus = new BuildingCard(2, Era.I, "BL_01", 3, 10, new SameInventorPairBonus(3));
    private final BuildingCard foodPerCompleteSet = new BuildingCard(2, Era.I, "BL_01", 3, 10, new FoodPerCompleteSet(5, 6));

    @Test
    @DisplayName("getter characterCardList")
    void getCharacterCardList() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(List.copyOf(buildingCardList), List.copyOf(characterCardList));

        assertEquals(characterCardList, tribe.getCharacterCardList());
    }

    @Test
    @DisplayName("getter buildingCardList")
    void getBuildingCardList() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(List.copyOf(buildingCardList), List.copyOf(characterCardList));

        assertEquals(buildingCardList, tribe.getBuildingCardList());
    }

    @Test
    @DisplayName("add character")
    void addCharacter1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(List.copyOf(buildingCardList), List.copyOf(characterCardList));

        tribe.addCharacter(artist);
        characterCardList.add(artist);

        assertEquals(characterCardList, tribe.getCharacterCardList());
    }

    @Test
    @DisplayName("addCharacter with empty building list returns 0 food bonus")
    void addCharacter2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        int food = tribe.addCharacter(artist);

        assertEquals(0, food);
    }

    @Test
    @DisplayName("addCharacter: pair of inventor triggers SameInventorPairBonus, returns 3 food bonus")
    void addCharacter3(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        int food = 0;
        food += tribe.addCharacter(inventorBowl);
        tribe.addBuilding(sameInventorPairBonus);
        food += tribe.addCharacter(inventorBowl);

        assertEquals(3, food);
    }

    @Test
    @DisplayName("addCharacter: first pair of inventor triggers before SameInventorPairBonus, returns 0 food")
    void addCharacter4(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        int food = 0;
        food += tribe.addCharacter(inventorBowl);
        food += tribe.addCharacter(inventorBowl);
        tribe.addBuilding(sameInventorPairBonus);
        food += tribe.addCharacter(inventorBowl);

        assertEquals(0, food);
    }

    @Test
    @DisplayName("addCharacter: one pair of inventor before and after SameInventorPairBonus building, returns 3 food")
    void addCharacter5(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        int food = 0;
        food += tribe.addCharacter(inventorBowl);
        food += tribe.addCharacter(inventorBowl);
        tribe.addBuilding(sameInventorPairBonus);
        food += tribe.addCharacter(inventorBowl);
        food += tribe.addCharacter(inventorBowl);

        assertEquals(3, food);
    }

    @Test
    @DisplayName("addCharacter: two pair of inventor after SameInventorPairBonus building, returns 6 food")
    void addCharacter6(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        int food = 0;
        food += tribe.addCharacter(inventorBowl);
        food += tribe.addCharacter(inventorArrow);
        tribe.addBuilding(sameInventorPairBonus);
        food += tribe.addCharacter(inventorBowl);
        food += tribe.addCharacter(inventorArrow);

        assertEquals(6, food);
    }

    @Test
    @DisplayName("addCharacter: 1 set before and after foodPerCompleteSet building, returns 5 food")
    void addCharacter7(){
        Tribe tribe = new Tribe(List.of(), List.of());
        Player p = new Player("Anna", tribe, 0,0,TotemColour.WHITE, true);
        artist.addToTribe(p);
        inventorBowl.addToTribe(p);
        hunter.addToTribe(p);
        gatherer.addToTribe(p);
        builder.addToTribe(p);
        shaman.addToTribe(p);
        foodPerCompleteSet.addToTribe(p);
        artist.addToTribe(p);
        inventorBowl.addToTribe(p);
        hunter.addToTribe(p);
        gatherer.addToTribe(p);
        builder.addToTribe(p);
        shaman.addToTribe(p);

        assertEquals(8, p.getFoodToken());
    }

    @Test
    @DisplayName("add building")
    void addBuilding() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(List.copyOf(buildingCardList), List.copyOf(characterCardList));

        tribe.addBuilding(extraShamanicStar);
        buildingCardList.add(extraShamanicStar);

        assertEquals(buildingCardList, tribe.getBuildingCardList());
    }

    @Test
    @DisplayName("get total character")
    void getTotalCharactersCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(characterCardList.size(), tribe.getTotalCharactersCount());
    }

    @Test
    @DisplayName("get total character after adding a character")
    void getTotalCharactersCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(List.copyOf(buildingCardList), List.copyOf(characterCardList));

        tribe.addCharacter(artist);
        characterCardList.add(artist);

        assertEquals(characterCardList.size(), tribe.getTotalCharactersCount());
    }

    @Test
    @DisplayName("1 extra up card pick with building")
    void getExtraCardPicksCount1(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getExtraCardPicksCount());
        assertEquals(1, tribe.getExtraUpCardPicksCount());
        assertEquals(0, tribe.getExtraDownCardPicksCount());
    }

    @Test
    @DisplayName("no extra card pick building")
    void getExtraCardPicksCount2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getExtraCardPicksCount());
        assertEquals(0, tribe.getExtraUpCardPicksCount());
        assertEquals(0, tribe.getExtraDownCardPicksCount());
    }

    @Test
    @DisplayName("no extra card pick building, empty building list")
    void getExtraCardPicksCount3(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getExtraCardPicksCount());
        assertEquals(0, tribe.getExtraUpCardPicksCount());
        assertEquals(0, tribe.getExtraDownCardPicksCount());
    }

    @Test
    @DisplayName("1 up 1 down extra card pick building")
    void getExtraCardPicksCount4(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extra1Up1DownCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getExtraCardPicksCount());
        assertEquals(1, tribe.getExtraUpCardPicksCount());
        assertEquals(1, tribe.getExtraDownCardPicksCount());
    }

    @Test
    @DisplayName("1 down extra card pick building")
    void getExtraCardPicksCount5(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extra1DownCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getExtraCardPicksCount());
        assertEquals(0, tribe.getExtraUpCardPicksCount());
        assertEquals(1, tribe.getExtraDownCardPicksCount());
    }

    @Test
    @DisplayName("2 extra food on turn end building card")
    void getExtraTurnOrderBonus1(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getExtraTurnOrderBonus());
    }

    @Test
    @DisplayName("no extra food on turn end building card")
    void getExtraTurnOrderBonus2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getExtraTurnOrderBonus());
    }

    @Test
    @DisplayName("no extra food on turn end, empty building card list")
    void getExtraTurnOrderBonus3(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getExtraTurnOrderBonus());
    }

    @Test
    @DisplayName("1 builder = 5 pp, no double pp building card")
    void getFinalBuilderPoints1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(5, tribe.getFinalBuilderPoints());
    }

    @Test
    @DisplayName("2 builder = 2*5 pp, no double pp building card")
    void getFinalBuilderPoints2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(10, tribe.getFinalBuilderPoints());
    }

    @Test
    @DisplayName("no builders, empty building card list")
    void getFinalBuilderPoints3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getFinalBuilderPoints());
    }

    @Test
    @DisplayName("empty character list, empty building card list")
    void getFinalBuilderPoints4() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getFinalBuilderPoints());
    }

    @Test
    @DisplayName("2 builder = 2*5 pp, with double builder points building")
    void getFinalBuilderPoints5() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleBuilderPoints, sustenanceArtistDiscount));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(20, tribe.getFinalBuilderPoints());
    }

    @Test
    @DisplayName("no gatherers, no sustenance discount building")
    void getTotalSustenanceDiscount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalSustenanceDiscount());
    }

    @Test
    @DisplayName("2 gatherers = 2*3, no sustenance discount building")
    void getTotalSustenanceDiscount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(6, tribe.getTotalSustenanceDiscount());
    }

    @Test
    @DisplayName("empty character card list, empty building card list")
    void getTotalSustenanceDiscount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalSustenanceDiscount());
    }

    @Test
    @DisplayName("no gatherers, sustenance artist discount building")
    void getTotalSustenanceDiscount4() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleBuilderPoints, sustenanceArtistDiscount));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalSustenanceDiscount());
    }

    @Test
    @DisplayName("1 gatherer = 3, sustenance gatherer and inventor discount building")
    void getTotalSustenanceDiscount5() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, shaman, builder, inventorBowl, gatherer, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(sustenanceGathererDiscount, sustenanceInventorDiscount));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(5, tribe.getTotalSustenanceDiscount());
    }

    @Test
    @DisplayName("1 hunter with hunt event bonus building")
    void getTotalBuildingFoodOnHuntEvent1(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(huntEventBonus, cavePaintingBonus));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getTotalBuildingFoodOnHuntEvent());
    }

    @Test
    @DisplayName("1 hunter without hunt event bonus building")
    void getTotalBuildingFoodOnHuntEvent2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalBuildingFoodOnHuntEvent());
    }

    @Test
    @DisplayName("1 hunter with hunt event bonus building")
    void getTotalBuildingPointsOnHuntEvent1(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(huntEventBonus, cavePaintingBonus));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getTotalBuildingPointsOnHuntEvent());
    }

    @Test
    @DisplayName("1 hunter without hunt event bonus building")
    void getTotalBuildingPointsOnHuntEvent2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalBuildingPointsOnHuntEvent());
    }

    @Test
    @DisplayName("1 extra shamanic star building = 3")
    void getAdditionalShamanicStars1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(3, tribe.getAdditionalShamanicStars());
    }

    @Test
    @DisplayName("no extra shamanic star building")
    void getAdditionalShamanicStars2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getAdditionalShamanicStars());
    }

    @Test
    @DisplayName("empty building card list")
    void getAdditionalShamanicStars3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getAdditionalShamanicStars());
    }

    @Test
    @DisplayName("no shamans, extra shamanic star building, no pp gain")
    void applyShamanicGain1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.applyShamanicGain(0, 3, 10, 1));
    }

    @Test
    @DisplayName("no shamans, extra shamanic star building, 5 pp gain")
    void applyShamanicGain2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(5, tribe.applyShamanicGain(5, 3, 3, 1));
    }

    @Test
    @DisplayName("2 shamans = 4 stars, double shamanic points, 5*2 pp gain")
    void applyShamanicGain3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(10, tribe.applyShamanicGain(5, 4, 4, 1));
    }

    @Test
    @DisplayName("no shamans, no shaman shield building, 5 pp loss")
    void applyShamanicLoss1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(5, tribe.applyShamanicLoss(5, 3, 5, 3));
    }

    @Test
    @DisplayName("not at min stars, no pp loss")
    void applyShamanicLoss2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.applyShamanicLoss(0, 3, 5, 1));
    }

    @Test
    @DisplayName("at min stars, with shamanic shield, no pp loss")
    void applyShamanicLoss3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.applyShamanicLoss(5, 4, 10, 4));
    }

    @Test
    @DisplayName("no shamans")
    void getShamanStarsCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getShamanStarsCount());
    }

    @Test
    @DisplayName("2 shamans = 4 stars")
    void getShamanStarsCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(4, tribe.getShamanStarsCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getShamanStarsCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getShamanStarsCount());
    }

    @Test
    @DisplayName("2 artists with cave painting bonus building card")
    void getBuildingArtistBonusFood1(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(huntEventBonus, cavePaintingBonus));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getBuildingArtistBonusFood());
    }

    @Test
    @DisplayName("2 artists with empty building card list")
    void getBuildingArtistBonusFood2(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getBuildingArtistBonusFood());
    }

    @Test
    @DisplayName("empty character card list with cave painting event building card")
    void getBuildingArtistBonusFood3(){
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(huntEventBonus, cavePaintingBonus));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getBuildingArtistBonusFood());
    }

    @Test
    @DisplayName("2 artists without cave painting event building")
    void getBuildingArtistBonusFood4(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getBuildingArtistBonusFood());
    }

    @Test
    @DisplayName("no artists with cave painting event building")
    void getBuildingArtistBonusFood5(){
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(huntEventBonus, cavePaintingBonus));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getBuildingArtistBonusFood());
    }

    @Test
    @DisplayName("2 inventor with equal invention icon")
    void getDistinctInventionsCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getDistinctInventionsCount());
    }

    @Test
    @DisplayName("2 inventors with 2 different invention icon")
    void getDistinctInventionsCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorArrow));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getDistinctInventionsCount());
    }

    @Test
    @DisplayName("no inventors")
    void getDistinctInventionsCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getDistinctInventionsCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getDistinctInventionsCount4() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getDistinctInventionsCount());
    }

    @Test
    @DisplayName("2 inventors with the same invention icon")
    void getEqualInventionsCoupleCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(doubleShamanicPoints, extra1UpCardPick));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getEqualInventionsCoupleCount());
    }

    @Test
    @DisplayName("2 inventors with different invention icon")
    void getEqualInventionsCoupleCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorArrow));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getEqualInventionsCoupleCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getEqualInventionsCoupleCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>();
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getEqualInventionsCoupleCount());
    }

    @Test
    @DisplayName("1 builder = 3 discount")
    void getBuildingDiscount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(3, tribe.getBuildingDiscount());
    }

    @Test
    @DisplayName("2 builders = 2*3 discount")
    void getBuildingDiscount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(6, tribe.getBuildingDiscount());
    }

    @Test
    @DisplayName("empty character card list")
    void getBuildingDiscount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(shamanicShield, doubleShamanicPoints));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getBuildingDiscount());
    }

    @Test
    @DisplayName("no sets of 6 cards")
    void getFullSetsOfCharacter1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getFullSetsOfCharacter(6));
    }

    @Test
    @DisplayName("2 set of 6 cards")
    void getFullSetsOfCharacter2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, inventorBowl, hunter, gatherer, builder, shaman));
        Tribe tribe = new Tribe(List.of(), List.of());
        Player p = new Player("Anna", tribe, 0,0,TotemColour.WHITE, true);

        for(int i = 0; i < 2; i++){
            for(CharacterCard characterCard : characterCardList){
                characterCard.addToTribe(p);
            }
        }

        assertEquals(2, tribe.getFullSetsOfCharacter(6));
    }

    @Test
    @DisplayName("no sets of 6 cards, 7 artists test")
    void getFullSetsOfCharacter3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, artist, artist, artist, artist, artist));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getFullSetsOfCharacter(6));
    }

    @Test
    @DisplayName("2 artists in character card list")
    void getTotalArtistCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalArtistCount());
    }

    @Test
    @DisplayName("1 artists in character card list")
    void getTotalArtistCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getTotalArtistCount());
    }

    @Test
    @DisplayName("no artists in character card list")
    void getTotalArtistCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalArtistCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalArtistCount4() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalArtistCount());
    }

    @Test
    @DisplayName("1 builder in character card list")
    void getTotalBuilderCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getTotalBuilderCount());
    }

    @Test
    @DisplayName("2 builder in character card list")
    void getTotalBuilderCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalBuilderCount());
    }

    @Test
    @DisplayName("no builder in character card list")
    void getTotalBuilderCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalBuilderCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalBuilderCount4() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalBuilderCount());
    }

    @Test
    @DisplayName("no gatherer in character card list")
    void getTotalGathererCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalGathererCount());
    }

    @Test
    @DisplayName("2 gatherers in character card list")
    void getTotalGathererCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman,  gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);
        assertEquals(2, tribe.getTotalGathererCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalGathererCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);
        assertEquals(0, tribe.getTotalGathererCount());
    }

    @Test
    @DisplayName("no hunter in character card list")
    void getTotalHunterCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalHunterCount());
    }

    @Test
    @DisplayName("1 hunter in character card list")
    void getTotalHunterCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(builder, builder, artist, hunter));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(1, tribe.getTotalHunterCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalHunterCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalHunterCount());
    }

    @Test
    @DisplayName("2 inventors in character card list")
    void getTotalInventorCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalInventorCount());
    }

    @Test
    @DisplayName("2 inventors in character card list")
    void getTotalInventorCount2() {
        List<CharacterCard> characterCardList = new  ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorArrow));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalInventorCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalInventorCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalInventorCount());
    }

    @Test
    @DisplayName("no shaman in character card list")
    void getTotalShamanCount1() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(artist, artist, builder, inventorBowl, inventorBowl));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalShamanCount());
    }

    @Test
    @DisplayName("2 shaman in character card list")
    void getTotalShamanCount2() {
        List<CharacterCard> characterCardList = new ArrayList<>(List.of(shaman, shaman, gatherer, gatherer));
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(2, tribe.getTotalShamanCount());
    }

    @Test
    @DisplayName("empty character card list")
    void getTotalShamanCount3() {
        List<CharacterCard> characterCardList = new ArrayList<>();
        List<BuildingCard> buildingCardList = new ArrayList<>(List.of(extraShamanicStar, extraFoodTurnEnd));
        Tribe tribe = new Tribe(buildingCardList, characterCardList);

        assertEquals(0, tribe.getTotalShamanCount());
    }
}