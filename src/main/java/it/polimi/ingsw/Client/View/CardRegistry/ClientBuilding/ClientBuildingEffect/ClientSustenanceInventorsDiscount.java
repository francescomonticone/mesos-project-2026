package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the food discount per Inventor during Sustenance event effect.
 */
public record ClientSustenanceInventorsDiscount(String type, int food) implements ClientBuildingEffect {
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
