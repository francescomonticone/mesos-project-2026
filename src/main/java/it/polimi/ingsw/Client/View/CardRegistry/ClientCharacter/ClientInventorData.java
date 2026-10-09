package it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.InventionIcon;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Inventor character card data.
 */
public record ClientInventorData(String id, String type, Era era, int minPlayers,
                                 InventionIcon invention) implements ClientCharacterData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
