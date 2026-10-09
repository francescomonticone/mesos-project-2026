package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OfferTrackTest {
    private OfferTrack offerTrack;
    private final int numPlayers = 3;

    @BeforeEach
    void setup(){
        offerTrack = new OfferTrack(numPlayers);
    }

    @Test
    @DisplayName("Default constructor test")
    void constructor(){
        assertNotNull(offerTrack.getTiles());
        assertFalse(offerTrack.getTiles().isEmpty());
        assertEquals(numPlayers+2, offerTrack.getTiles().size()); // 7 is the number of tiles associated with 5 players

        // All tiles should initially be free
        for (OfferTile tile : offerTrack.getTiles()) {
            assertTrue(tile.isFree(), "Tile " + tile.getTileId() + " should be free at initialization");
        }

        List<Character> availableIds = offerTrack.getAvailableTiles();
        assertEquals(offerTrack.getTiles().size(), availableIds.size(), "All tiles should be available initially");
    }

    @Test
    @DisplayName("Standard placeTotem method behaviour")
    void place() {
        // Setup
        Player player = new Player("Anna");
        char targetTileId = offerTrack.getTiles().getFirst().getTileId();

        // Simulate
        offerTrack.placeTotem(player, targetTileId);

        // Assert
        assertFalse(offerTrack.isTileFree(targetTileId));
        OfferTile occupiedTile = offerTrack.getTile(player);
        assertNotNull(occupiedTile);
        assertEquals(targetTileId, occupiedTile.getTileId());
    }

    @Test
    @DisplayName("If placing on an invalid tile ID OfferTrack should throw an IllegalArgumentException")
    void exception() {
        // Setup
        Player player = mock(Player.class);
        char invalidTileId = '-';

        // Simulate & Assert
        assertThrows(IllegalArgumentException.class, () -> offerTrack.isTileFree(invalidTileId));
        assertThrows(IllegalArgumentException.class, () -> offerTrack.placeTotem(player, invalidTileId));

    }

    @Test
    @DisplayName("If player has not placed a totem, accessing the tile occupied by said player should result in an" +
            "IllegalStateException")
    void exception2() {
        // Setup
        Player player = new Player("Anna");

        // Simulate & Assert
        assertThrows(IllegalStateException.class, () -> offerTrack.getTile(player));
    }

    @Test
    @DisplayName("Standard releaseTotem method behaviour")
    void release() {
        // Setup
        Player player = new Player("Anna");
        char targetTileId = offerTrack.getTiles().getFirst().getTileId();
        offerTrack.placeTotem(player, targetTileId);
        assertFalse(offerTrack.isTileFree(targetTileId));

        // Simulate
        offerTrack.releaseTotem(player);

        // Assert
        assertTrue(offerTrack.isTileFree(targetTileId));
        assertThrows(IllegalStateException.class, () -> offerTrack.getTile(player));
    }

    @Test
    @DisplayName("If trying to release a player that has not placed the totem no exception are thrown")
    void release2() {
        // Setup
        Player player = mock(Player.class);

        // Simulate & Assert
        assertDoesNotThrow(() -> offerTrack.releaseTotem(player));
    }

    @Test
    @DisplayName("Standard getPlayersLeftToRight behaviour")
    void order() {
        // Setup
        Player player1 = mock(Player.class);
        Player player2 = mock(Player.class);
        List<OfferTile> tiles = offerTrack.getTiles();

        char firstTileId = tiles.getFirst().getTileId();
        char secondTileId = tiles.get(1).getTileId();

        // Simulate: Place player2 on the first physical tile, and player1 on the second
        offerTrack.placeTotem(player1, secondTileId);
        offerTrack.placeTotem(player2, firstTileId);

        List<Player> orderedPlayers = offerTrack.getPlayersLeftToRight();

        // Assert
        assertEquals(2, orderedPlayers.size());
        assertEquals(player2, orderedPlayers.get(0));
        assertEquals(player1, orderedPlayers.get(1));
    }

}