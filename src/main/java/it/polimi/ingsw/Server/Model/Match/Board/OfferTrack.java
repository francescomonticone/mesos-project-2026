package it.polimi.ingsw.Server.Model.Match.Board;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Represents the physical Offer Track component on the game board.
 * <p>
 * This class manages the collection of {@link OfferTile}s available in the current match.
 * It handles the placement and retrieval of player totems, keeping a mapping between
 * each player and their chosen tile. The order of the tiles dynamically dictates the
 * turn order for the card-picking resolution phase.
 * </p>
 */
public class OfferTrack {
    private final List<OfferTile> offerTiles;
    private final Map<Player, OfferTile> playerPositions;

    /**
     * Constructs the OfferTrack for a match, creating the correct set of
     * OfferTiles based on the number of players.
     *
     * @param numPlayers the number of players, used to select the correct tiles
     */
    public OfferTrack(int numPlayers) {
        this.offerTiles = new ArrayList<>();
        createTrack(numPlayers);
        for(OfferTile tile: offerTiles) tile.release();
        this.playerPositions = new HashMap<>();
    }

    /**
     * Populates the offer track by reading from a JSON configuration file.
     * <p>
     * Uses a {@link RuntimeTypeAdapterFactory} to properly deserialize the polymorphic
     * {@link TileMode} (the strategy pattern determining if a tile gives Food or Card Picks).
     * Tiles are only added if their minimum player requirement is met.
     * </p>
     *
     * @param numPlayer the total number of players in the match
     */
    private void createTrack(int numPlayer){
        Type targetClassType = new TypeToken<ArrayList<OfferTile>>(){ }.getType();
        RuntimeTypeAdapterFactory<TileMode> tileModeAdapter = RuntimeTypeAdapterFactory
                .of(TileMode.class, "type")
                .registerSubtype(FoodGain.class, "FoodGain")
                .registerSubtype(CardPick.class, "CardPick");


        Gson gson = new GsonBuilder().registerTypeAdapterFactory(tileModeAdapter).create();

        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/offerTiles.json")))){
            List<OfferTile> loaded = gson.fromJson(reader, targetClassType);
            for(OfferTile tile: loaded){
                if(numPlayer >= tile.getMinPlayers()){
                    offerTiles.add(tile);
                }
            }
        }catch(Exception e){
            System.err.println("Failed to load offer tiles into Server");
        }
    }

    /**
     * Returns whether the tile identified by {@code tileId} is free.
     *
     * @param tileId the letter identifier of the tile
     * @return true if no totem is placed on the tile
     * @throws IllegalArgumentException if {@code tileId} does not match any tile
     */
    public boolean isTileFree(char tileId) {
        return getExistingTile(tileId).isFree();
    }

    /**
     * Returns the tile currently occupied by the given player.
     *
     * @param player the player to query
     * @return the {@link OfferTile} occupied by {@code player}
     * @throws IllegalStateException if the player has no totem on the track
     */
    public OfferTile getTile(Player player) {
        OfferTile tile = playerPositions.get(player);
        if (tile == null)
            throw new IllegalStateException(player.getNickname() + " has no totem on the offer track");
        return tile;
    }

    /**
     * Returns the players who have placed their totem on the offer track,
     * ordered left-to-right based on their tile position.
     *
     * <p>The order is determined by the position of each {@link OfferTile}
     * in the offer track list, which reflects the physical left-to-right
     * arrangement of the tiles. Players who have not yet placed their totem
     * are excluded from the result.</p>
     *
     * @return an unmodifiable list of players in left-to-right tile order,
     *         containing only players with an active totem on the track
     */
    public List<Player> getPlayersLeftToRight() {
        return offerTiles.stream()
                .map(tile -> playerPositions.entrySet().stream()//creates a stream of a set of entries
                        .filter(e -> e.getValue().equals(tile)) //if the tile corresponds
                        .map(Map.Entry::getKey) //take the key (Player)
                        .findFirst()
                )
                .filter(Optional::isPresent) //only tiles that have a player return a non-empty Optional
                .map(Optional::get) //extract stream of players
                .toList();
    }
    /**
     * Releases the totem from the track at the end of a Player's round.
     */
    public void releaseTotem(Player player) {
        OfferTile tile = playerPositions.remove(player); //remove the player from the map and get the tile
        if (tile != null) {
            tile.release(); //free the tile if it was occupied
        }
    }

    /**
     * Releases the totem from the track at the end of a Player's round.
     */
    public void placeTotem(Player player, char tileId) {
        OfferTile tile = getExistingTile(tileId);
        tile.occupy(player); // throws IllegalStateException if it is already occupied
        playerPositions.put(player, tile); //insert the player in the map
    }


    public List<OfferTile> getTiles() {
        return List.copyOf(offerTiles);
    }

    /**
     * Scans the offer track and retrieves a list of all currently unoccupied tiles.
     * Used primarily for bot-fallback logic when a player disconnects.
     *
     * @return a list of characters representing the IDs of free tiles
     */
    public List<Character> getAvailableTiles() { //List of char
        return offerTiles.stream()
                .filter(OfferTile::isFree)      // Keep only the tiles that are free
                .map(OfferTile::getTileId)      // Extract the character ID from those tiles
                .toList();                      // Collect them into a List
    }


    //private helpers
    /**
     * Retrieves the tile with the given letter identifier.
     *
     * @param tileId the letter identifier of the tile
     * @return the matching {@link OfferTile}
     * @throws IllegalArgumentException if no tile with that ID exists
     */
    private OfferTile getExistingTile(char tileId) {
        return offerTiles.stream()
                .filter(t -> t.getTileId() == tileId) //search for the correct tile
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No tile with ID: " + tileId));
    }

}
