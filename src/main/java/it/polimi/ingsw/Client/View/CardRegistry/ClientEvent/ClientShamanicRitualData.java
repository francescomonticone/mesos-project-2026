package it.polimi.ingsw.Client.View.CardRegistry.ClientEvent;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Shamanic ritual event card data.
 */
public record ClientShamanicRitualData(String id, Era era, String className, int minPlayers, boolean isFinalEvent,
                                       int prestigePointsGain, int prestigePointsLoss) implements ClientEventData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
