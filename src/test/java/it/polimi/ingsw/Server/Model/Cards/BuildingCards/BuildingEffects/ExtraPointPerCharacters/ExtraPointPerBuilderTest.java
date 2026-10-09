package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.ExtraPointPerCharacters;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingCard;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.*;
import it.polimi.ingsw.Server.Model.Match.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraPointPerBuilderTest {

    @Test
    void onEndOfGame() {
        // Setup
        BuildingCard test = new BuildingCard(2,null,null,0,0,
                new ExtraPointPerBuilder(2));

        Player p = new Player("Anna");

        CharacterCard artist = new Artist(2, null, null, 1, "ARTIST");
        CharacterCard builder = new Builder(2, null, null, 1, 0, "BUILDER");

        // setup player tribe
        artist.addToTribe(p);
        builder.addToTribe(p);
        builder.addToTribe(p);

        // Simulate
        int res = test.getEffect().onEndOfGame(p.getTribe());

        // Assert
        assertEquals(3, p.getTribe().getCharacterCardList().size());
        assertEquals(4, res);
    }
}