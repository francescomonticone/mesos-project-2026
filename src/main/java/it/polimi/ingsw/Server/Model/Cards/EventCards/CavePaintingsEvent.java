package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.BuildingCards.BuildingEffects.CavePaintingBonus;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the Cave Painting event card in the game.
 * During this event, players are evaluated based on the number of equivalent Artists in their {@link Tribe}.
 * This ensures that future game expansions—such as introducing a
 * "Master Artist" or a wildcard(jolly) character worth multiple artists—are seamlessly supported without needing to modify
 * this event's underlying code.
 * Depending on the thresholds defined by the specific era's card:
 * <ul>
 *     <li>If the number of artists is less than or equal to the loss threshold, the player loses prestige points.</li>
 *     <li>If the number of artists meets or exceeds the gain threshold, the player gains prestige points multiplied by their artist count.</li>
 * </ul>
 *
 * Any additional effects (e.g., gaining food per artist) provided by building cards
 * are resolved separately via the controller and the BuildingEffect class.
 *
 * @see EventCard
 * @see EventResult
 * @see CavePaintingBonus
 */

public class CavePaintingsEvent extends EventCard{
    private final int lossThreshold;
    private final int prestigePointsLost;
    private final int gainThreshold;
    private final int prestigePointsPerArtist;

    public CavePaintingsEvent(String id, Era era, int minPlayers, boolean isFinalEvent, int lossThreshold, int prestigePointsLost, int gainThreshold, int prestigePointsPerArtist) {
        super(id, era, minPlayers, isFinalEvent);
        this.lossThreshold = lossThreshold;
        this.prestigePointsLost = prestigePointsLost;
        this.gainThreshold = gainThreshold;
        this.prestigePointsPerArtist = prestigePointsPerArtist;
    }

    /**
     * Calculates the prestige point deltas for each {@link Player} during the Cave Painting event.
     * <p>
     * The method determines the total number of equivalent Artists in each player's {@link Tribe}.
     * It then applies either a prestige penalty if the count is less than or equal to the
     * {@code lossThreshold}, or a prestige reward (proportional to the number of Artists) if the
     * count meets or exceeds the {@code gainThreshold}.
     * </p>
     * <p>
     * Food resources are strictly unaffected by the base event calculation and will always yield a delta of 0.
     * </p>
     * <p>
     * Any side effects related to food generation provided by building cards must be applied
     * by the EventResolutionState.
     * </p>
     *
     * @param players the list of players participating in the event; must not be {@code null} or empty
     * @return an {@link EventResult} containing the food and prestige point deltas for each player
     * @throws IllegalArgumentException if the provided list of players is {@code null} or empty
     */

    @Override
    public EventResult calculate (List<Player> players){

        if (players == null || players.isEmpty())
            throw new IllegalArgumentException("Players list must not be null or empty");

        Map<Player, Integer> foodDeltas = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();

        for(Player p : players){
            int nArtist = p.getTribe().getTotalArtistCount();

            if(nArtist <= lossThreshold)
                prestigeDeltas.put(p, -prestigePointsLost);
            else if(nArtist >= gainThreshold)
                prestigeDeltas.put(p, prestigePointsPerArtist*nArtist);
            else
                prestigeDeltas.put(p, 0);

            foodDeltas.put(p, p.getTribe().getBuildingArtistBonusFood());
        }

        return new EventResult(prestigeDeltas, foodDeltas);
    }
}
