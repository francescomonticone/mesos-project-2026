package it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Builder character card data.
 */
public record ClientBuilderData(String id, String type, Era era, int minPlayers,
                                int buildingDiscount, int prestigePoints) implements ClientCharacterData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
