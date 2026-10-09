package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

/**
 * Building effect that grants extra shamanic stars during the ritual event.
 */
public class ExtraShamanicStar extends BuildingEffect{
    private final int stars;

    public ExtraShamanicStar(int stars) {
        this.stars = stars;
    }

    /**
     * Returns the extra shamanic stars granted by this effect.
     *
     * @return the number of extra shamanic stars
     */
    @Override
    public int getExtraShamanicStars() {
        return stars;
    }

}
