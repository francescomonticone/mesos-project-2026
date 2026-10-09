package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Represents the building effect that triggers during the Hunt event.
 *
 * <p>According to Mesos rules, this building grants the player an additional
 * amount of food and prestige points proportional to the number of
 * Hunters in their tribe during the Hunt event.
 *
 */

public class HuntEventBonus extends BuildingEffect{

    private final int food;
    private final int points;

    public HuntEventBonus(int food, int points) {
        this.food = food;
        this.points = points;
    }

    /**
     * Returns the prestige points gained during the Hunt event.
     *
     * @param tribe the tribe owning the building
     * @return the prestige points gained from the Hunt event
     */
    @Override
    public int getPointsOnHuntEvent(Tribe tribe){
        int nHunters = tribe.getTotalHunterCount();
        return points*nHunters;
    }

    /**
     * Returns the food gained during the Hunt event.
     *
     * @param tribe the tribe owning the building
     * @return the food gained from the Hunt event
     */
    @Override
    public int getFoodOnHuntEvent(Tribe tribe){
        int nHunters = tribe.getTotalHunterCount();
        return food*nHunters;
    }

}
