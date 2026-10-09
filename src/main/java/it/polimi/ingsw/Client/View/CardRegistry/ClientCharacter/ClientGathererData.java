package it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Gatherer character card data.
 */
public record ClientGathererData(String id, String type, Era era, int minPlayers,
                                 int foodDiscount) implements ClientCharacterData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
