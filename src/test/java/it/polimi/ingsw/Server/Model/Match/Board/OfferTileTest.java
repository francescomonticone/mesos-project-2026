package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;
import net.bytebuddy.pool.TypePool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OfferTileTest {
    private OfferTile tile;

    @BeforeEach
    void setup(){
        tile = new OfferTile('A', 5, new FoodGain(3));
    }

    @Test
    @DisplayName("Standard occupy method behaviour")
    void occupy(){
        // Setup
        Player p = new Player("Anna");

        // Simulate
        tile.occupy(p);

        // Assert
        assertFalse(tile.isFree());
        assertSame(p, tile.getOccupant());
    }

    @Test
    @DisplayName("occupy method should throw an IllegalStateException when the tile is already occupied")
    void occupy2(){
        // Setup
        Player p1 = new Player("Anna");
        Player p2 = new Player("Bruno");

        // Simulate
        tile.occupy(p1);

        // Assert
        assertThrows(IllegalStateException.class, () -> {
            tile.occupy(p2);
        });
    }

    @Test
    @DisplayName("Standard release method behaviour")
    void release(){
        // Setup
        Player p = new Player("Anna");
        tile.occupy(p);

        // Simulate
        tile.release();

        // Assert
        assertTrue(tile.isFree());
        assertThrows(IllegalStateException.class, () ->{
            tile.getOccupant();
        });
    }


}