package it.polimi.ingsw.Server.Model.CardFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Server.Model.Cards.EventCards.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Factory class responsible for loading, storing, and providing {@link EventCard} instances.
 * <p>
 * This class reads event card definitions from a JSON configuration file ({@code events.json})
 * during initialization. It utilizes a polymorphic approach to instantiate specific subclasses
 * of {@code EventCard} based on their class name specified in the JSON data.
 * </p>
 */
public class EventCardFactory{

    /**
     * A map storing all loaded events cards, where the key is the card ID
     * and the value is the corresponding {@link EventCard} instance.
     */
    private final Map<String, EventCard> eventMap;

    /**
     * Constructs a new {@code EventCardFactory}.
     * Initializes the internal map and triggers the JSON loading process.
     */
    public EventCardFactory() {
        this.eventMap = new HashMap<>();
        loadCardsFromJson();
    }

    /**
     * Loads character cards from the {@code /JSON/events.json} resource file.
     * <p>
     * This method uses a {@link RuntimeTypeAdapterFactory} to handle the polymorphic
     * deserialization of {@link EventCard} objects. Depending on the value of the
     * "className" field in the JSON, Gson will instantiate the correct subclass (e.g.,
     * {@link SustenanceEvent}, {@link ShamanicRitualEvent}, etc.).
     * </p>
     */
    private void loadCardsFromJson() {
        // Define the target type for Gson deserialization (a List of EventCards)
        Type targetClassType = new TypeToken<ArrayList<EventCard>>(){ }.getType();

        // Configure the polymorphic adapter.
        // It reads the "className" property from the JSON to determine which subclass to instantiate.
        RuntimeTypeAdapterFactory<EventCard> eventAdapter = RuntimeTypeAdapterFactory
                .of(EventCard.class, "className", true)
                .registerSubtype(CavePaintingsEvent.class, "CavePaintingsEvent")
                .registerSubtype(HuntEvent.class, "HuntEvent")
                .registerSubtype(ShamanicRitualEvent.class, "ShamanicRitualEvent")
                .registerSubtype(SustenanceEvent.class, "SustenanceEvent");

        // Build the Gson instance registering the custom adapter
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(eventAdapter).create();

        // Use try-with-resources to ensure the Reader is closed automatically after reading.
        // It safely retrieves the JSON file as a resource stream from the classpath.
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/events.json")))){
            List<EventCard> loaded = gson.fromJson(reader, targetClassType); // Deserialize the JSON into a list of CharacterCard objects
            for(EventCard card: loaded){
                eventMap.put(card.getId(), card); // Populate the map using the card ID as the key
            }
        }catch(Exception e){
            System.err.println("Failed to load event cards into Sever");
        }
    }


    /**
     * Returns all non-final Event cards filtered by player count,
     * used by {@link MainDeckBuilder} to assemble the main deck.
     *
     * @return list of non-final EventCards
     */
    public List<EventCard> getAllNonFinalCards() {
        return eventMap.values().stream()
                .filter(e -> !e.getIsFinalEvent())
                .collect(Collectors.toList());
    }

    /**
     * Returns the Final Event cards to be placed at the bottom of the main deck.
     *
     * @return list of Final EventCards
     */
    public List<EventCard> getFinalCards() {
        return eventMap.values().stream()
                .filter(EventCard::getIsFinalEvent)
                .collect(Collectors.toList());
    }
}