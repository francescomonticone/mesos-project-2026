package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the Bonus gained during Hunt event effect.
 */
public record ClientHuntEventBonus(String type, int food, int points) implements ClientBuildingEffect {
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
