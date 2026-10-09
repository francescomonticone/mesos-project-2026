package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.SustenanceDiscounts;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Sustenance discount effect that reduces the food cost based on the number
 * of artists in the tribe.
 */
public class SustenanceArtistDiscount extends SustenanceDiscount{

    private final int food;

    /**
     * Creates a sustenance discount based on artists.
     *
     * @param food the food discount applied for each artist; must be non-negative
     * @throws IllegalArgumentException if {@code food} is negative
     */
    public SustenanceArtistDiscount(int food){
        if(food < 0)
            throw new IllegalArgumentException("food discount must be positive, the sign will be considered in SustenanceEvent");
        this.food = food;
    }

    /**
     * Calculates the total sustenance discount for the tribe based on the number of artists.
     *
     * @param tribe the tribe to evaluate
     * @return the total food discount contribution
     */
    @Override
    public int onSustenanceEvent(Tribe tribe){
        return food * tribe.getTotalArtistCount();
    }
}
