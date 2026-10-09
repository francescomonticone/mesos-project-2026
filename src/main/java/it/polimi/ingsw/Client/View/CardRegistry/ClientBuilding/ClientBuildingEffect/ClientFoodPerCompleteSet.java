package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the extra food gained when completing a set of characters effect.
 */
public record ClientFoodPerCompleteSet(String type, int food, int setSize) implements ClientBuildingEffect {
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
