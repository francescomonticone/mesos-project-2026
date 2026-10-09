package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Building effect that grants food when new complete sets are formed.
 */
public class FoodPerCompleteSet extends BuildingEffect{
    private final int food;
    private final int setSize;
    private int previousSets = 0; //how many complete sets before onBuildingAcquired();


    public FoodPerCompleteSet(int food, int setSize) {
        this.food = food;
        this.setSize = setSize;
    }

    /**
     * Stores the number of complete sets already present when the building is acquired.
     *
     * @param tribe the tribe owning the building
     */
    @Override
    public void onBuildingAcquired(Tribe tribe) {
        this.previousSets = tribe.getFullSetsOfCharacter(setSize);
    }

    /**
     * Returns the food gained for any new complete sets formed after acquisition.
     *
     * @param tribe the tribe owning the building
     * @return the food gained from newly completed sets
     */
    @Override
    public int onCardPickFoodBonus(Tribe tribe) {
        int currentSets = tribe.getFullSetsOfCharacter(setSize);
        if (currentSets > previousSets) {
            int result = (currentSets - previousSets) * this.food;
            previousSets = currentSets;
            return result;
        }
        return 0;
    }

}
