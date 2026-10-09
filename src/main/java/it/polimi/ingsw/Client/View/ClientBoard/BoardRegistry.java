package it.polimi.ingsw.Client.View.ClientBoard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Network.DTO.MatchDTO;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A registry class that manages the client-side database of game tiles.
 * It is responsible for loading data from external JSON files
 * and providing access to them via their unique identifiers.
 * <p>The class uses Google GSON with {@code RuntimeTypeAdapterFactory} to handle
 * the polymorphic deserialization of different card categories.</p>
 *
 * @author Gabriele Maiolo
 */
public class BoardRegistry {
    private final Map<Character, ClientOfferTile> offerTrack; //Map to store offer tiles
    private final List<Integer> foodBonus;

    /**
     * Initializes the registry and triggers the loading process for all tiles.
     */
    public BoardRegistry(MatchDTO match){
        this.offerTrack = new HashMap<>();
        this.foodBonus = new ArrayList<>(initializeFoodBonuses(match.players().size()));
        loadTiles(match.players().size());
    }

    /**
     * Loads character tile data from the "offerTiles.json" file.
     * Configures a polymorphic adapter to distinguish between different tile actions
     */
    private void loadTiles(int numPlayer){
        Type targetClassType = new TypeToken<ArrayList<ClientOfferTile>>(){ }.getType();
        RuntimeTypeAdapterFactory<ClientTileMode> tileModeAdapter = RuntimeTypeAdapterFactory
                .of(ClientTileMode.class, "type")
                .registerSubtype(ClientFoodGain.class, "FoodGain")
                .registerSubtype(ClientCardPick.class, "CardPick");


        Gson gson = new GsonBuilder().registerTypeAdapterFactory(tileModeAdapter).create();

        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/offerTiles.json")))){
            List<ClientOfferTile> loaded = gson.fromJson(reader, targetClassType);
            for(ClientOfferTile tile: loaded){
                if(numPlayer >= tile.minPlayers()){
                    offerTrack.put(tile.tileId(), tile);
                }
            }
        }catch(Exception e){
            System.err.println("Failed to load offer tiles into Server");
        }
    }

    /**
     * Initializes the list of food bonuses for the current match configuration.
     *
     * <p>The method deserializes the turn order tiles from the JSON resource file and
     * returns the food bonuses of the tile whose number of players matches the input.
     * If the resource cannot be read or no matching tile exists, an empty list is returned.</p>
     *
     * @param numPlayers the number of players in the match
     * @return the list of food bonuses for the matching turn order tile, or an empty list if none is available
     */
    private List<Integer> initializeFoodBonuses(int numPlayers) {
        Type targetClassType = new TypeToken<ArrayList<ClientTurnOrderTile>>(){ }.getType();
        Gson gson =  new Gson();
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/turnOrderTiles.json")))){
            List<ClientTurnOrderTile> loaded = gson.fromJson(reader, targetClassType);
            for(ClientTurnOrderTile tile: loaded){
                if(numPlayers == tile.numPlayers()){
                    return new ArrayList<>(tile.foodBonus());
                }
            }
        }catch(Exception e){
            System.err.println("Failed to load turn order tiles into Client");
        }
        return List.of();
    }

    /**
     * Method to retrieve offer track
     *
     * @return HashMap containing the offer track based on the number of player in a game
     */
    public Map<Character, ClientOfferTile> getOfferTrack(){
        return new HashMap<>(offerTrack);
    }


    /**
     * Returns a defensive copy of the food bonuses for this tile.
     *
     * @return a copy of the food bonus list
     */
    public List<Integer> getOrderFoodBonus(){ return new ArrayList<>(foodBonus);}
}
