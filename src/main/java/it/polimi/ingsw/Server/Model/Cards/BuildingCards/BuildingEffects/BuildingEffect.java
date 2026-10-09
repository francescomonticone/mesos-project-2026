package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Base class for all building card effects.
 *
 * <p>This class defines a set of hook methods that can be overridden by subclasses
 * to react to specific game events. Unless otherwise specified, each method provides
 * a neutral default behavior.</p>
 */
public abstract class BuildingEffect{

    /**
     * Computes the food bonus granted when a card is picked.
     *
     * @param tribe the tribe affected by the effect
     * @return the food bonus granted on card pick, or {@code 0} if no bonus applies
     * */
    public int onCardPickFoodBonus(Tribe tribe){return 0;}

    /**
     * Computes the sustenance discount or contribution during a sustenance event.
     *
     * @param tribe the tribe affected by the effect
     * @return the sustenance contribution, or {@code 0} if no effect applies
     */
    public int onSustenanceEvent(Tribe tribe){return 0;}

    /**
     * Adjusts the gain of shamanic stars.
     *
     * @param currentGain the current computed gain
     * @param ownerStars the number of stars of the owner
     * @param maxStars the maximum number of stars among players
     * @param maxGroupCount the number of players tied for the maximum
     * @return the adjusted shamanic gain
     */
    public int onShamanicGain(int currentGain, int ownerStars, int maxStars, int maxGroupCount) {
        return currentGain;
    }

    /**
     * Adjusts the loss of shamanic stars.
     *
     * @param currentLoss the current computed loss
     * @param ownerStars the number of stars of the owner
     * @param maxStars the maximum number of stars among players
     * @param minStars the minimum number of stars among players
     * @return the adjusted shamanic loss
     */
    public int onShamanicLoss(int currentLoss, int ownerStars, int maxStars, int minStars) {
        return currentLoss;
    }

    /**
     * Computes the bonus granted during a cave painting event.
     *
     * @param tribe the tribe affected by the effect
     * @return the event contribution, or {@code 0} if no effect applies
     */
    public int onCavePaintingEvent(Tribe tribe){return 0;}

    /**
     * Computes the bonus points granted at the end of the game.
     *
     * @param tribe the tribe affected by the effect
     * @return the end-game bonus, or {@code 0} if no bonus applies
     */
    public int onEndOfGame(Tribe tribe){return 0;}

    /**
     * Applies side effects when the related building is acquired.
     *
     * @param tribe the tribe acquiring the building
     */
    public void onBuildingAcquired(Tribe tribe) {} //default: no operations

    //getters
    public int getExtraShamanicStars() {
        return 0;
    } //Shamanic is not an attribute of the player but depends on the cards

    public int getShamanicMultiplier() {
        return 1;
    }

    public double getFinalBuilderPointsMultiplier() {
        return 1.0;
    }

    public int howManyExtraCardPicks(){
        return 0; // Default: 0 extra card picks, can be overridden by subclasses
    }

    public int getExtraUpCardPicks(){
        return 0;
    }

    public int getExtraDownCardPicks(){
        return 0;
    }

    public int getExtraTurnOrderFoodBonus() {
        return 0; // Default: 0 extra food, can be overridden by subclasses
    }

    public int getPointsOnHuntEvent(Tribe tribe){
        return 0;
    }

    public int getFoodOnHuntEvent(Tribe tribe){
        return 0;
    }
}