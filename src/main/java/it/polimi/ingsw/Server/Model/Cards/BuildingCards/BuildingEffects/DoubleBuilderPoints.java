package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Represents a building effect that doubles the final points of builder cards.
 *
 * <p>According to the Mesos rules, some buildings modify the final end-game
 * scoring of Builders by applying a multiplier.</p>
 */
public class DoubleBuilderPoints extends BuildingEffect{
//Calling this MultiplierBuilderPoints? The effect would be generic

    /**
     * Returns the multiplier applied to the final builder points.
     *
     * @return {@code 2.0}, because this effect doubles the builder points
     */
    @Override
    public double getFinalBuilderPointsMultiplier() {
        return 2.0;
    } //Doubles points during the final Result computation

}

