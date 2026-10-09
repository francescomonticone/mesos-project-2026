package it.polimi.ingsw.Client.View.CardRegistry.ClientEvent;

import it.polimi.ingsw.Client.View.CardRegistry.ClientCardData;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Interface representing event-specific card data.
 */
public interface ClientEventData extends ClientCardData {
    String id();
    Era era();
    String className();
    int minPlayers();
    boolean isFinalEvent();
}
