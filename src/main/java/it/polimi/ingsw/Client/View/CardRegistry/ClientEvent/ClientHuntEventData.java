package it.polimi.ingsw.Client.View.CardRegistry.ClientEvent;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Hunt event card data.
 */
public record ClientHuntEventData(String id, Era era, String className, int minPlayers, boolean isFinalEvent,
                                  int prestigePointsBonus, int huntFoodBonus) implements ClientEventData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
