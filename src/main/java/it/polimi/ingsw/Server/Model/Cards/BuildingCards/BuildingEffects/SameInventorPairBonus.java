package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Building effect that grants food when the tribe forms new pairs
 * of inventors with the same invention icon.
 */
public class SameInventorPairBonus extends BuildingEffect{
    private final int food;
    private int previousCouples = 0; //how many Inventor couples before the Building activation



    public SameInventorPairBonus(int food) {
        this.food = food;
    }

    /**
     * Stores the number of matching inventor pairs already present when the building is acquired.
     *
     * @param tribe the tribe owning the building
     */
    @Override
    public void onBuildingAcquired(Tribe tribe) {
        this.previousCouples = tribe.getEqualInventionsCoupleCount(); //sets initial couples counter
    }

    /**
     * Returns the food gained from new matching inventor pairs formed after the building was acquired.
     *
     * @param tribe the tribe owning the building
     * @return the food gained from newly formed matching inventor pairs
     */
    @Override
    public int onCardPickFoodBonus(Tribe tribe) {
        int currentCouples = tribe.getEqualInventionsCoupleCount();
        if (currentCouples > previousCouples) { // if the number of couples increases
            int result = (currentCouples - previousCouples) * this.food; //calculate the food to give
            previousCouples = currentCouples; //update the number of effective couples
            return result;
        }
        return 0;
    }
}
