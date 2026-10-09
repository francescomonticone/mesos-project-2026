package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Building effect that prevents prestige point loss during the Shamanic Ritual
 * when the owner has the lowest number of shamanic stars.
 */
public class ShamanicShield extends BuildingEffect{

    /**
     * Prevents shamanic prestige point loss when the owner is among the players
     * with the minimum number of stars.
     *
     * @param currentLoss the currently computed prestige point loss
     * @param ownerStars the number of stars of the building owner
     * @param maxStars the maximum number of stars among players
     * @param minStars the minimum number of stars among players
     * @return {@code 0} if the loss is prevented, otherwise the current loss
     */
    @Override
    public int onShamanicLoss(int currentLoss, int ownerStars, int maxStars, int minStars) {
        if (ownerStars == minStars) {
            return 0; // shield absorbs the loss
        }
        return currentLoss;
    }
}
