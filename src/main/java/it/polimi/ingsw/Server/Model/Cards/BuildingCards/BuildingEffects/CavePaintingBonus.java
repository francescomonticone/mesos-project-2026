package it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects;

import it.polimi.ingsw.Server.Model.Match.Tribe;

/**
 * Represents the specific building effect that triggers during the Cave Painting event.
 *
 * <p>According to Mesos rules, this building grants the player an amount of food
 * proportional to the number of Artists in their tribe during the Cave Painting event.
 *
 */
public class CavePaintingBonus extends BuildingEffect {

    private final int foodPerArtist;


    public CavePaintingBonus(int foodPerArtist) {
        this.foodPerArtist = foodPerArtist;
    }

    /**
     * Computes the food bonus granted during the Cave Painting event.
     *
     * @param tribe the tribe affected by the effect
     * @return the total amount of food awarded, equal to the number of artists
     *         in the tribe multiplied by the food bonus for each artist
     */
    @Override
    public int onCavePaintingEvent(Tribe tribe){
        int nArtists = tribe.getTotalArtistCount();
        return foodPerArtist * nArtists;
    }
}