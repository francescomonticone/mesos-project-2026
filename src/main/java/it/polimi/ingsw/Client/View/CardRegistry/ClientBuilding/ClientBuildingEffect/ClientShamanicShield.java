package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the effect that prevents prestige points loss during Shamanic ritual event.
 */
public record ClientShamanicShield(String type) implements ClientBuildingEffect {
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
