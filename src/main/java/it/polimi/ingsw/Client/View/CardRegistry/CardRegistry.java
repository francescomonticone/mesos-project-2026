package it.polimi.ingsw.Client.View.CardRegistry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientEvent.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingData;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A registry class that manages the client-side database of game cards.
 * It is responsible for loading character and event card data from external JSON files
 * and providing access to them via their unique identifiers.
 * <p>The class uses Google GSON with {@code RuntimeTypeAdapterFactory} to handle
 * the polymorphic deserialization of different card categories.</p>
 *
 * @author Gabriele Maiolo
 */
public class CardRegistry {
    private final Map<String, ClientCardData> cards; //Map storing the loaded cards, indexed by their unique ID

    /**
     * Initializes the registry and triggers the loading process for all card types.
     */
    public CardRegistry(){
        this.cards = new HashMap<>();
        loadCharacters();
        loadEvent();
        loadBuilding();
    }

    /**
     * Loads character card data from the "characters.json" file.
     * Configures a polymorphic adapter to distinguish between different character
     * types such as ARTIST, BUILDER, HUNTER, etc.
     */
    private void loadCharacters(){
        Type targetClassType = new TypeToken<ArrayList<ClientCharacterData>>(){ }.getType();
        RuntimeTypeAdapterFactory<ClientCharacterData> characterAdapter = RuntimeTypeAdapterFactory
                .of(ClientCharacterData.class, "type", true)
                .registerSubtype(ClientArtistData.class, "ARTIST")
                .registerSubtype(ClientBuilderData.class, "BUILDER")
                .registerSubtype(ClientGathererData.class, "GATHERER")
                .registerSubtype(ClientHunterData.class, "HUNTER")
                .registerSubtype(ClientInventorData.class, "INVENTOR")
                .registerSubtype(ClientShamanData.class, "SHAMAN");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(characterAdapter).create();

        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/characters.json")))){
            List<ClientCharacterData> loaded = gson.fromJson(reader, targetClassType);
            for(ClientCharacterData card: loaded){
                cards.put(card.id(), card);
            }

        }catch(Exception e){
            System.err.println("Failed to load character cards into Client");
        }
    }

    /**
     * Loads event card data from the "events.json" file.
     * Configures a polymorphic adapter to handle various event subclasses like
     * HuntEvent or ShamanicRitualEvent.
     */
    private void loadEvent(){
        Type targetClassType = new TypeToken<ArrayList<ClientEventData>>(){ }.getType();
        RuntimeTypeAdapterFactory<ClientEventData> eventAdapter = RuntimeTypeAdapterFactory
                .of(ClientEventData.class, "className", true)
                .registerSubtype(ClientCavePaintingsEventData.class, "CavePaintingsEvent")
                .registerSubtype(ClientHuntEventData.class, "HuntEvent")
                .registerSubtype(ClientShamanicRitualData.class, "ShamanicRitualEvent")
                .registerSubtype(ClientSustenanceEventData.class, "SustenanceEvent");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(eventAdapter).create();

        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/events.json")))){
            List<ClientEventData> loaded = gson.fromJson(reader, targetClassType);
            for(ClientEventData card: loaded){
                cards.put(card.id(), card);
            }
        }catch(Exception e){
            System.err.println("Failed to load event cards into Client");
        }
    }

    /**
     * Loads building card data from the "buildings.json" file.
     * Configures a polymorphic adapter to handle various event subclasses like
     * HuntEvent or ShamanicRitualEvent.
     */
    private void loadBuilding(){
        Type targetClassType = new TypeToken<ArrayList<ClientBuildingData>>(){ }.getType();
        RuntimeTypeAdapterFactory<ClientBuildingEffect> effectAdapter = RuntimeTypeAdapterFactory
                .of(ClientBuildingEffect.class, "type", true)
                .registerSubtype(ClientFoodPerCompleteSet.class, "FoodPerCompleteSet")
                .registerSubtype(ClientSustenanceGatherersDiscount.class, "SustenanceGatherersDiscount")
                .registerSubtype(ClientSustenanceArtistDiscount.class, "SustenanceArtistDiscount")
                .registerSubtype(ClientShamanicShield.class, "ShamanicShield")
                .registerSubtype(ClientExtraFoodTurnEnd.class, "ExtraFoodTurnEnd")
                .registerSubtype(ClientSameInventorPairBonus.class, "SameInventorPairBonus")
                .registerSubtype(ClientDoubleShamanicPoints.class, "DoubleShamanicPoints")
                .registerSubtype(ClientExtraShamanicStar.class, "ExtraShamanicStar")
                .registerSubtype(ClientSustenanceInventorsDiscount.class, "SustenanceInventorsDiscount")
                .registerSubtype(ClientHuntEventBonus.class, "HuntEventBonus")
                .registerSubtype(ClientDoubleBuilderPoints.class, "DoubleBuilderPoints")
                .registerSubtype(ClientCavePaintingBonus.class, "CavePaintingBonus")
                .registerSubtype(ClientPointsPerCompleteSet.class, "PointsPerCompleteSet")
                .registerSubtype(ClientExtraPointPerHunter.class, "ExtraPointPerHunter")
                .registerSubtype(ClientExtraPointPerGatherer.class, "ExtraPointPerGatherer")
                .registerSubtype(ClientExtraPointPerShaman.class, "ExtraPointPerShaman")
                .registerSubtype(ClientExtraPointPerBuilder.class, "ExtraPointPerBuilder")
                .registerSubtype(ClientExtraPointPerArtist.class, "ExtraPointPerArtist")
                .registerSubtype(ClientExtraPointPerInventor.class, "ExtraPointPerInventor")
                .registerSubtype(ClientExtraCardPick.class, "ExtraCardPick")
                .registerSubtype(ClientBonusPoints.class, "BonusPoints");


        Gson gson = new GsonBuilder().registerTypeAdapterFactory(effectAdapter).create();

        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/buildings.json")))){
            List<ClientBuildingData> loaded = gson.fromJson(reader, targetClassType);
            for(ClientBuildingData card: loaded){
                cards.put(card.id(), card);
            }

        }catch(Exception e){
            System.err.println("Failed to load character cards into Server");
        }
    }

    /**
     * Retrieves a card from the registry based on its ID.
     *
     * @param id The unique identifier of the card.
     * @return An {@link Optional} containing the card data if found, or empty otherwise.
     */
    public Optional<ClientCardData> getCard(String id){
        return Optional.ofNullable(cards.get(id));
    }
}
