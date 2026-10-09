package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the extra food at turn end effect.
 */
public record ClientExtraFoodTurnEnd(String type, int food) implements  ClientBuildingEffect{
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
