package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.*;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointsPerCompleteSetTest {

    @Test
    void onEndOfGame() {
        // Setup
        BuildingCard test = new BuildingCard(2, null, "BL_01", 0, 0,
                new PointsPerCompleteSet(1, 6));

        CharacterCard artist = new Artist(2, null, null, 0, "ARTIST");
        CharacterCard builder = new Builder(2, null, null, 0, 0, "BUILDER");
        CharacterCard gatherer = new Gatherer(2, null, null, 0, "GATHERER");
        CharacterCard hunter = new Hunter(2, null, null, true, 1, "HUNTER");
        CharacterCard inventor = new Inventor(2, null, null, null, "INVENTOR");
        CharacterCard shaman = new Shaman(2, null, null, 0, "SHAMAN");

        List<CharacterCard> completeSet = new ArrayList<>(List.of(artist, builder, gatherer, hunter, inventor, shaman));

        Player p = new Player("Anna");

        // Simulate & Assert
        int res0 = test.getEffect().onEndOfGame(p.getTribe());
        assertEquals(0, res0);

        for(CharacterCard c : completeSet){
            c.addToTribe(p);
        }
        int res1 = test.getEffect().onEndOfGame(p.getTribe());
        assertEquals(1, res1);

        for(CharacterCard c: completeSet){
            c.addToTribe(p);
        }
        int res2 = test.getEffect().onEndOfGame(p.getTribe());
        assertEquals(2, res2);
    }
}