package it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter;

import it.polimi.ingsw.Client.View.CardRegistry.ClientCardData;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Interface representing character-specific card data.
 */
public interface ClientCharacterData
extends ClientCardData {
    String id();
    String type();
    Era era();
    int minPlayers();
}
