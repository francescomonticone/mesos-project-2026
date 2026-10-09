package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Building effect that grants extra food when the player's totem is placed
 * on a bonus space of the turn order tile.
 */
public class ExtraFoodTurnEnd extends BuildingEffect{
    private final int food;

    public ExtraFoodTurnEnd(int food) {
        this.food = food;
    }

    /**
     * Returns the extra food bonus granted by the turn order tile effect.
     *
     * @return the extra food bonus
     */
    @Override
    public int getExtraTurnOrderFoodBonus() {
        return food;
    }
}