package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Sustenance discount effect that reduces the food cost based on the number
 * of gatherers in the tribe.
 */
public class SustenanceGatherersDiscount extends SustenanceDiscount{
    private final int food;

    /**
     * Creates a sustenance discount based on gatherers.
     *
     * @param food the food discount applied for each gatherer; must be non-negative
     * @throws IllegalArgumentException if {@code food} is negative
     */
    public SustenanceGatherersDiscount(int food){
        if(food < 0)
            throw new IllegalArgumentException("food discount must be positive, the sign will be considered in SustenanceEvent");
        this.food = food;
    }

    /**
     * Calculates the total sustenance discount for the tribe based on the number of gatherers.
     *
     * @param tribe the tribe to evaluate
     * @return the total food discount contribution
     */
    @Override
    public int onSustenanceEvent(Tribe tribe){
        return food * tribe.getTotalGathererCount();
    }
}
