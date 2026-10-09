package it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect;

/**
 * Represents the extra prestige points gained per complete set of character at the end of the game effect.
 */
public record ClientPointsPerCompleteSet(String type, int points, int setSize) implements ClientBuildingEffect {
    @Override
    public <T> T accept(ClientEffectVisitor<T> visitor){
        return visitor.visit(this);
    }
}
