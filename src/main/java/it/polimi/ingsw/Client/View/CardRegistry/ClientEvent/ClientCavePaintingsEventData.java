package it.polimi.ingsw.Client.View.CardRegistry.ClientEvent;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for Cave paintings event card data.
 */
public record ClientCavePaintingsEventData(String id, Era era, String className, int minPlayers, boolean isFinalEvent,
                                           int lossThreshold, int prestigePointsLost, int gainThreshold,
                                           int prestigePointsPerArtist) implements ClientEventData{
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
