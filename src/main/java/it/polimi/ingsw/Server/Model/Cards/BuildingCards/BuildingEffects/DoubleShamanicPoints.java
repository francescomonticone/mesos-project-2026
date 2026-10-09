package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Building effect that doubles shamanic gain when the player has the unique majority.
 */
public class DoubleShamanicPoints extends BuildingEffect{

    @Override
    public int getShamanicMultiplier() { return 2; }

    /**
     * Doubles shamanic gain only if the player has the unique majority of stars.
     *
     * @param currentGain the current computed gain
     * @param ownerStars the number of stars of the owner
     * @param maxStars the maximum number of stars among players
     * @param maxGroupCount the number of players tied for the maximum
     * @return the doubled gain if the player has the unique majority, otherwise the original gain
     */
    @Override
    public int onShamanicGain(int currentGain, int ownerStars, int maxStars, int maxGroupCount) {
        // implementation follows the physical version rule where the points
        // are not doubled if the player does not have more stars than all other players
        boolean hasOnlyMajority = (ownerStars == maxStars) && (maxGroupCount == 1);
        return hasOnlyMajority ? currentGain * getShamanicMultiplier() : currentGain; //if player has Majority it returns the multiplied bonus stars
    }
}
