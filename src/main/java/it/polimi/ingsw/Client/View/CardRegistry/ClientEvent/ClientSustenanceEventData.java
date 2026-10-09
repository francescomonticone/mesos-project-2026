package it.polimi.ingsw.Client.View.CardRegistry.ClientEvent;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Sustenance event card data.
 */
public record ClientSustenanceEventData(String id, Era era, String className, int minPlayers, boolean isFinalEvent,
                                        int prestigePointsCost, int foodCost) implements ClientEventData{
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
