package it.polimi.ingsw.Server.Model.CardFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.*;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters.*;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceArtistDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceGatherersDiscount;
import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts.SustenanceInventorsDiscount;
import it.polimi.ingsw.Server.Model.Cards.Deck;
import it.polimi.ingsw.Server.Model.Match.Era;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory responsible for loading and sharing {@link BuildingCard} instances.
 * <p>
 * Implements the Flyweight pattern to ensure only one instance of each building
 * card exists in memory. It also provides utilities to split building decks by Era.
 * </p>
 */
public class BuildingCardFactory{

    /**
     * A map storing all loaded building cards, where the key is the card ID
     * and the value is the corresponding {@link BuildingCard} instance.
     */
    private final Map<String, BuildingCard> buildingMap;

    private static final int[][] SETUP_TABLE = { //the first column is zero only for easier indexing
            {0, 0, 0, 0}, // Index 0: 0 players (not used, but added for easier indexing)
            {0, 0, 0, 0}, // Index 1: 1 player  (not used, but added for easier indexing)
            {0, 1, 2, 3}, // Index 2: 2 players -> Era 1: 1 | Era 2: 2 | Era 3: 3
            {0, 2, 2, 4}, // Index 3: 3 players -> Era 1: 2 | Era 2: 2 | Era 3: 4
            {0, 2, 3, 4}, // Index 4: 4 players -> Era 1: 2 | Era 2: 3 | Era 3: 4
            {0, 2, 3, 5}  // Index 5: 5 players -> Era 1: 2 | Era 2: 3 | Era 3: 5
    };

    /**
     * Constructs a new {@code BuildingCardFactory}.
     * Initializes the internal map and triggers the JSON loading process.
     */
    public BuildingCardFactory() {
        this.buildingMap = new HashMap<>();
        loadCardsFromJson();
    }

    /**
     * Loads character cards from the {@code /JSON/buildings.json} resource file.
     * <p>
     * This method uses a {@link RuntimeTypeAdapterFactory} to handle the polymorphic
     * deserialization of {@link BuildingCard} objects. Depending on the value of the
     * "type" field in the JSON, Gson will instantiate the correct {@link BuildingEffect} (e.g.,
     * {@link FoodPerCompleteSet}, {@link BonusPoints}, etc.).
     * </p>
     */
    private void loadCardsFromJson() {
        // Define the target type for Gson deserialization (a List of BuildingCards)
        Type targetClassType = new TypeToken<ArrayList<BuildingCard>>(){ }.getType();

        // Configure the polymorphic adapter.
        // It reads the "type" property from the JSON to determine which subclass to instantiate.
        RuntimeTypeAdapterFactory<BuildingEffect> effectAdapter = RuntimeTypeAdapterFactory
                .of(BuildingEffect.class, "type", true)
                .registerSubtype(FoodPerCompleteSet.class, "FoodPerCompleteSet")
                .registerSubtype(SustenanceGatherersDiscount.class, "SustenanceGatherersDiscount")
                .registerSubtype(SustenanceArtistDiscount.class, "SustenanceArtistDiscount")
                .registerSubtype(ShamanicShield.class, "ShamanicShield")
                .registerSubtype(ExtraFoodTurnEnd.class, "ExtraFoodTurnEnd")
                .registerSubtype(SameInventorPairBonus.class, "SameInventorPairBonus")
                .registerSubtype(DoubleShamanicPoints.class, "DoubleShamanicPoints")
                .registerSubtype(ExtraShamanicStar.class, "ExtraShamanicStar")
                .registerSubtype(SustenanceInventorsDiscount.class, "SustenanceInventorsDiscount")
                .registerSubtype(HuntEventBonus.class, "HuntEventBonus")
                .registerSubtype(DoubleBuilderPoints.class, "DoubleBuilderPoints")
                .registerSubtype(CavePaintingBonus.class, "CavePaintingBonus")
                .registerSubtype(PointsPerCompleteSet.class, "PointsPerCompleteSet")
                .registerSubtype(ExtraPointPerHunter.class, "ExtraPointPerHunter")
                .registerSubtype(ExtraPointPerGatherer.class, "ExtraPointPerGatherer")
                .registerSubtype(ExtraPointPerShaman.class, "ExtraPointPerShaman")
                .registerSubtype(ExtraPointPerBuilder.class, "ExtraPointPerBuilder")
                .registerSubtype(ExtraPointPerArtist.class, "ExtraPointPerArtist")
                .registerSubtype(ExtraPointPerInventor.class, "ExtraPointPerInventor")
                .registerSubtype(ExtraCardPick.class, "ExtraCardPick")
                .registerSubtype(BonusPoints.class, "BonusPoints");


        // Build the Gson instance registering the custom adapter
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(effectAdapter).create();

        // Use try-with-resources to ensure the Reader is closed automatically after reading.
        // It safely retrieves the JSON file as a resource stream from the classpath.
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/buildings.json")))){
            List<BuildingCard> loaded = gson.fromJson(reader, targetClassType); // Deserialize the JSON into a list of CharacterCard objects
            for(BuildingCard card: loaded){
                buildingMap.put(card.getId(), card); // Populate the map using the card ID as the key
            }

        }catch(Exception e){
            System.err.println("Failed to load character cards into Server");
        }
    }

    /**
     * Creates shuffled building decks grouped by their respective Era.
     * The number of cards in each deck depends on the number of players,
     * according to the official game rules table.
     *
     * @param numPlayers the number of players in the match
     * @return a map containing a customized, shuffled Deck of BuildingCards for each Era
     */
    public Map<Era, Deck<BuildingCard>> createBuildingDecksByEra(int numPlayers) {
        Map<Era, Deck<BuildingCard>> decksByEra = new EnumMap<>(Era.class);

        // Group the shared Flyweight instances by Era
        Map<Era, List<BuildingCard>> groupedCards = buildingMap.values().stream()
                .collect(Collectors.groupingBy(BuildingCard::getEra));

        // Shuffle each group, apply the limit, and wrap it in a Deck object
        for (Map.Entry<Era, List<BuildingCard>> entry : groupedCards.entrySet()) {
            Era era = entry.getKey();
            List<BuildingCard> eraCards = new ArrayList<>(entry.getValue());
            Collections.shuffle(eraCards); //shuffle the cards of this era

            int targetSize = getTargetSize(numPlayers, era, eraCards.size());

            //extracts the sublist
            List<BuildingCard> limitedCards = new ArrayList<>(eraCards.subList(0, targetSize)); //extract sublist from index 0 to targetSize (targetSize excluded!)

            decksByEra.put(era, new Deck<>(limitedCards));
        }

        return decksByEra;
    }

    /**
     * Helper method to compute the number of cards needed with respect to the official setup table.
     */
    private int getTargetSize(int numPlayers, Era era, int availableCards) {
        int eraValue = era.getValue(); // 1, 2, or 3

        //lookup of the table in the row numPlayers and column era, previously extracted
        int targetSize = SETUP_TABLE[numPlayers][eraValue];

        return Math.min(targetSize, availableCards);//if the JSON has fewer cards, this ensures we don't try to access out of bounds
    }

}