package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding;

import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect.ClientBuildingEffect;
import it.polimi.ingsw.Client.View.CardRegistry.ClientCardData;
import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

/**
 * Data record for building card data.
 */
public record ClientBuildingData(String id, Era era, int minPlayers,
                                 int foodCost, int finalPrestigePoints,
                                 ClientBuildingEffect effect) implements ClientCardData {
    @Override
    public <T> T accept(ClientCardVisitor<T> visitor){
        return visitor.visit(this);
    }
}
