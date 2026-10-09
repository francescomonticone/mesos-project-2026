package it.polimi.ingsw.Server.Model.Match.Board;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the Turn Order Tile physical component on the game board.
 * <p>
 * This class tracks the vertical arrangement of players' totems from top to bottom,
 * which determines both the initiative order for the next round and the specific
 * food bonuses awarded at the end of the current round.
 * </p>
 */
public class TurnOrderTile {
    private final int numPlayers;
    private List<Player> playerOrderTopBottom;
    private final List<Integer> foodBonus;

    /**
     * Constructs a new TurnOrderTile for the match.
     * Initializes the player order list and sets up the food bonus values
     * corresponding to each position on the tile.
     *
     * @param numPlayers the number of players, used to determine the size
     *                   of the bonus list and available spaces.
     */
    public TurnOrderTile(int numPlayers) {
        this.numPlayers = numPlayers;

        // Initialize the list that will track player totems from top to bottom
        this.playerOrderTopBottom = new ArrayList<>(numPlayers);

        // Initialize the food bonus list (fixed values based on the board design)
        this.foodBonus = new ArrayList<>(initializeFoodBonuses(numPlayers));
    }

    /**
     * Explicitly sets the top-to-bottom order of players on the tile.
     * <p>
     * This is typically used during the initial setup of the match or when
     * resolving complex order changes where sequential placement is insufficient.
     * </p>
     *
     * @param playerOrderTopBottom a list of {@link Player} objects representing the new order
     */
    public void setPlayerOrderTopBottom(List<Player> playerOrderTopBottom) {
        this.playerOrderTopBottom = new ArrayList<>(playerOrderTopBottom);
    }

    /**
     * Populates the foodBonus list with specific values by reading from a JSON configuration file.
     * <p>
     * It searches the internal {@code /JSON/turnOrderTiles.json} resource and parses the
     * correct configuration that matches the current number of players.
     * </p>
     *
     * @param numPlayers the number of players in the match.
     * @return a list of integers representing the food bonuses from top to bottom,
     * or an empty list if the configuration file cannot be loaded.
     */
    private List<Integer> initializeFoodBonuses(int numPlayers) {
        Type targetClassType = new TypeToken<ArrayList<TurnOrderTile>>(){ }.getType();
        Gson gson =  new Gson();
        try(Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/JSON/turnOrderTiles.json")))){
            List<TurnOrderTile> loaded = gson.fromJson(reader, targetClassType);
            for(TurnOrderTile tile: loaded){
                if(numPlayers == tile.getNumPlayers()){
                    return new ArrayList<>(tile.getFoodBonus());
                }
            }
        }catch(Exception e){
            System.err.println("Failed to load turn order tiles into Server");
        }
        return List.of();
    }

    private int getNumPlayers(){return numPlayers;}

    private List<Integer> getFoodBonus(){return foodBonus;}

    /**
     * Retrieves the current top-to-bottom order of players on the tile.
     *
     * @return a defensive copy of the player order list
     */
    public List<Player> getPlayerOrderTopBottom(){
        return new ArrayList<>(playerOrderTopBottom);
    }

    /**
     * Insert player in the first available slot of the TurnOrderTile
     *
     * @param player the player to insert in the tile
     * @return index of the tile occupied this way
     */
    public int placeTotemOnTurnOrderTile(Player player) {
        int index = playerOrderTopBottom.indexOf(null); //index of the first null value (free Tile space)
        if (index == -1)
            throw new IllegalStateException("No free space on the turn order tile");
        playerOrderTopBottom.set(index, player); //insert the player at the correct index
        return index;
    }

    /**
     * Resets all slots to null, ready to receive totem placements for the next round.
     * It is used for testing.
     */
    public void reset() { //used to
        Collections.fill(playerOrderTopBottom, null);
    }

    /**
     * Checks whether a specific slot index corresponds to the very bottom space on the tile.
     *
     * @param spaceIndex the zero-based index to check
     * @return {@code true} if the index is the last valid position, {@code false} otherwise
     */
    public boolean isLastSpace(int spaceIndex){
        return spaceIndex == playerOrderTopBottom.size() - 1;
    }

    /**
     * Retrieves the specific food bonus associated with a given slot index on the tile.
     *
     * @param index the zero-based index of the slot
     * @return the food bonus amount for that slot
     * @throws IndexOutOfBoundsException if the index is negative or greater than the number of available slots
     */
    public int getFoodBonusFromPosition(int index){
        if(index >= foodBonus.size()) throw new IndexOutOfBoundsException();
        return this.foodBonus.get(index);
    }

    /**
     * Removes the player totem from a specific slot on the tile by setting it to {@code null}.
     * <p>
     * Used dynamically when a player resolves their turn and retrieves their totem.
     * </p>
     *
     * @param index the zero-based index of the slot to clear
     */
    public void removeFromTurnOrderTile(int index){
        playerOrderTopBottom.set(index, null);
    }

}
