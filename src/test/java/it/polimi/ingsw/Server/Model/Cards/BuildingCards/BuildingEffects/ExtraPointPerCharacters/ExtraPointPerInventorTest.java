package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.*;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraPointPerInventorTest {

    @Test
    void onEndOfGame() {
        // Setup
        BuildingCard test = new BuildingCard(2,null,null,0,0,
                new ExtraPointPerInventor(2));

        Player p = new Player("Anna");

        CharacterCard builder = new Builder(2, null, null, 1, 0, "BUILDER");
        CharacterCard inventor = new Inventor(2, null, null, InventionIcon.BOAT, "INVENTOR");


        // setup player tribe
        inventor.addToTribe(p);
        inventor.addToTribe(p);
        builder.addToTribe(p);

        // Simulate
        int res = test.getEffect().onEndOfGame(p.getTribe());

        // Assert
        assertEquals(3, p.getTribe().getCharacterCardList().size());
        assertEquals(4, res);
    }
}