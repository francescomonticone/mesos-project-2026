package it.polimi.ingsw.Server.Model.CardFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Factory class responsible for loading and providing {@link CharacterCard} instances.
 * <p>
 * This class reads card data from a JSON configuration file ({@code characters.json})
 * upon instantiation and stores them in memory. It uses polymorphism to correctly
 * instantiate specific subclasses of {@code CharacterCard} based on their type.
 * </p>
 */
public class CharacterCardFactory{

    /**
     * A map storing all loaded character cards, where the key is the card ID
     * and the value is the corresponding {@link CharacterCard} instance.
     */
    private final Map<String, CharacterCard> characterMap;

    /**
     * Constructs a new {@code CharacterCardFactory}.
     * Initializes the internal map and triggers the JSON loading process.
     */
    public CharacterCardFactory(){
        this.characterMap = new HashMap<>();
        loadFromJSON();
    }

    /**
     * Loads character cards from the {@code /JSON/characters.json} resource file.
     * <p>
     * This method uses a {@link RuntimeTypeAdapterFactory} to handle the polymorphic
     * deserialization of {@link CharacterCard} objects. Depending on the value of the
     * "type" field in the JSON, Gson will instantiate the correct subclass (e.g.,
     * {@link Artist}, {@link Builder}, etc.).
     * </p>
     */
    private void loadFromJSON(){
        // Define the target type for Gson deserialization (a List of CharacterCards)
        Type targetClassType = new TypeToken<ArrayList<CharacterCard>>(){ }.getType();

        // Configure the polymorphic adapter.
        // It reads the "type" property from the JSON to determine which subclass to instantiate.
        RuntimeTypeAdapterFactory<CharacterCard> characterAdapter = RuntimeTypeAdapterFactory
                .of(CharacterCard.class, "type", true)
                .registerSubtype(Artist.class, "ARTIST")
                .registerSubtype(Builder.class, "BUILDER")
                .registerSubtype(Gatherer.class, "GATHERER")
                .registerSubtype(Hunter.class, "HUNTER")
                .registerSubtype(Inventor.class, "INVENTOR")
                .registerSubtype(Shaman.class, "SHAMAN");

        // Build the Gson instance registering the custom adapter
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(characterAdapter).create();

        // Use try-with-resources to ensure the Reader is closed automatically after reading.
        // It safely retrieves the JSON file as a resource stream from the classpath.
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/characters.json")))){
            List<CharacterCard> loaded = gson.fromJson(reader, targetClassType); // Deserialize the JSON into a list of CharacterCard objects
            for(CharacterCard card: loaded){
                characterMap.put(card.getId(), card); // Populate the map using the card ID as the key
            }

        }catch(Exception e){
            System.err.println("Failed to load character cards into Server");
        }
    }

    /**
     * Computes the number of distinct character types currently loaded in the factory.
     * * @return the count of unique character types (e.g., if there are multiple ARTIST cards,
     * it only counts as 1 distinct type).
     */
    public int computeDistinctCharactersCount() {
        return (int) characterMap.values().stream()
                .map(CharacterCard::getType)
                .distinct()
                .count();
    }

    /**
     * Retrieves a list of all loaded {@link CharacterCard} instances.
     * <p>
     * Note: This method returns a new {@code ArrayList} containing the values to
     * prevent external modification of the internal map's collection.
     * </p>
     * * @return a {@link List} containing all available character cards.
     */
    public List<CharacterCard> getAllCards() {
        return new ArrayList<>(characterMap.values());
    }

}