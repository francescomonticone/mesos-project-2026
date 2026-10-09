package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * Represents a single placement slot (tile) on the {@link OfferTrack}.
 * <p>
 * Each OfferTile acts as a container that holds a specific behavior defined by its
 * {@link TileMode} (the Strategy Pattern). It also manages its own occupation state,
 * tracking whether a player has placed their totem on it during the current round.
 * </p>
 */
public class OfferTile {
    private final char tileId;
    private final int minPlayers;
    private final TileMode tileMode;
    private boolean isFree; // true = free | false = occupied
    private Player occupant = null; //initially this is null, then it will be set to the player who occupies the tile

    /**
     * Constructs a new OfferTile with its specific identifier, requirement, and behavior.
     *
     * @param Id         the character identifier of the tile (e.g., 'A', 'B', 'C')
     * @param minPlayers the minimum number of players required in the match for this tile to be used
     * @param tileAction the {@link TileMode} strategy defining the tile's specific reward or effect
     */
    public OfferTile(char Id, int minPlayers, TileMode tileAction) {
        tileId = Id;
        this.minPlayers = minPlayers;
        this.tileMode = tileAction;
        this.isFree = true; //this will be true initially
    }

    public char getTileId(){
        return tileId;
    }

    public int getMinPlayers(){ return minPlayers;}

    //methods for tile mode invocation
    public int getUpperSlots(){
        return this.tileMode.getUpperRowCount();
    }
    public int getLowerSlots(){
        return this.tileMode.getLowerRowCount();
    }

    /**
     * Checks if the tile's effect resolves automatically without player interaction.
     * Delegates to the underlying {@link TileMode}.
     *
     * @return {@code true} if the effect is automatic, {@code false} otherwise
     */
    public boolean isAutomatic() {
        return tileMode.isAutomatic();
    }

    /**
     * Applies the instant effect of the tile to the specified player.
     * Delegates to the underlying {@link TileMode}.
     *
     * @param player the {@link Player} receiving the automatic effect
     */
    public void applyAutomaticEffect(Player player) {
        tileMode.applyAutomaticEffect(player);
    }

    /**
     * Retrieves the player currently occupying this tile.
     *
     * @return the {@link Player} whose totem is on this tile
     * @throws IllegalStateException if the tile is currently free
     */
    public Player getOccupant() {
        if(isFree) throw new IllegalStateException("Cannot get occupant because the tile it's free");
        return occupant;
    }

    //helpers

    /** @return true if no totem is currently placed on this tile */
    public boolean isFree() { return isFree; }

    /**
     * Marks this tile as occupied.
     *
     * @throws IllegalStateException if the tile is already occupied
     */
    public void occupy(Player player) {
        if (!isFree)
            throw new IllegalStateException("Tile " + tileId + " is already occupied");
        this.occupant = player;
        this.isFree = false;
    }

    /** Releases this tile for the next round. */
    public void release() { this.isFree = true; this.occupant = null; }

}
