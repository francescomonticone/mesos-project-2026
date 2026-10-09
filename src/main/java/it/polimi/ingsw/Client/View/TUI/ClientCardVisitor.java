package it.polimi.ingsw.Client.View.TUI;

import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingData;
import it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientEvent.*;

/**
 * Visitor interface for cards.
 * Defines visit methods for every concrete implementation of {@link it.polimi.ingsw.Client.View.CardRegistry.ClientCardData}.
 *
 * @param <T> The return type of the visit methods (e.g., List of Strings for rendering).
 */
public interface ClientCardVisitor<T> {
    T visit(ClientHunterData card);
    T visit(ClientBuilderData card);
    T visit(ClientGathererData card);
    T visit(ClientArtistData card);
    T visit(ClientInventorData card);
    T visit(ClientShamanData card);
    T visit(ClientHuntEventData card);
    T visit(ClientSustenanceEventData card);
    T visit(ClientShamanicRitualData card);
    T visit(ClientCavePaintingsEventData card);
    T visit(ClientBuildingData card); //tmp
}
