package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the "Shamanic Ritual" event card in the game.
 *
 * <p>Players are ranked by their effective shamanic star count (base stars + building bonuses).
 * The player(s) with the most stars gain prestige; the player(s) with the fewest stars lose prestige.
 * In case of a global tie (all players share the same star count), every player first gains
 * and then loses prestige — the two phases are kept separate to allow building effects
 * (e.g. ShamanicShield) to interact correctly.</p>
 *
 * <p>Building effects are applied via {@link Tribe#applyShamanicGain}
 * and {@link Tribe#applyShamanicLoss}, keeping all game logic
 * inside the Model layer.</p>
 *
 * @see EventCard
 * @see Tribe
 */
public class ShamanicRitualEvent extends EventCard {

    private final int prestigePointsGain;
    private final int prestigePointsLoss;

    public ShamanicRitualEvent(String id, Era era, int minPlayers, boolean isFinalEvent,
                               int prestigePointsGain, int prestigePointsLoss) {
        super(id, era, minPlayers, isFinalEvent);
        this.prestigePointsGain = prestigePointsGain;
        this.prestigePointsLoss = prestigePointsLoss;
    }

    /**
     * Calculates the net prestige changes for all players during the Shamanic Ritual.
     * <p><b>Global tie:</b> if all players share the same star count, every player is in both
     * the max and min group. The two phases are resolved independently so that building effects
     * can interact with each phase separately, as specified by the rulebook.</p>
     *
     * <p>Food deltas are always 0 for this event.</p>
     *
     * @param players the list of {@link Player}s participating in the event
     * @return an {@link EventResult} containing prestige and food deltas for each player
     * @throws IllegalArgumentException if the player list is null or empty
     */
    @Override
    public EventResult calculate(List<Player> players) {
        if (players == null || players.isEmpty())
            throw new IllegalArgumentException("Players list must not be null or empty");

        Map<Player, Integer> foodDeltas     = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();
        Map<Player, Integer> effectiveStars = new HashMap<>();

        // compute effective stars for each player
        for (Player p : players) { //if the player has buildings with shamanicBonuses
            int stars = p.getTribe().getShamanStarsCount() + p.getTribe().getAdditionalShamanicStars();
            effectiveStars.put(p, stars);
            foodDeltas.put(p, 0); //initialize food
            prestigeDeltas.put(p, 0);
        }
        //compute max e min stars after bonuses on additional stars
        int maxStars = effectiveStars.values().stream()
                .mapToInt(Integer::intValue).max().orElse(0);
        int minStars = effectiveStars.values().stream()
                .mapToInt(Integer::intValue).min().orElse(0);

        // how many players share the maximum (needed by DoubleShamanicPoints)
        int maxGroupCount = (int) effectiveStars.values().stream()
                                                .filter(s -> s == maxStars)
                                                .count();

        // gain phase : players with the most stars
        for (Player p : players) {
            int ownerStars = effectiveStars.get(p);
            if (ownerStars == maxStars) { //if p is in maxGroup
                int gain = p.getTribe().applyShamanicGain(prestigePointsGain, ownerStars, maxStars, maxGroupCount);
                prestigeDeltas.put(p, gain);
            }
        }

        // loss phase : players with the fewest stars
        for (Player p : players) {
            int ownerStars = effectiveStars.get(p);
            if (ownerStars == minStars) { //if p is in minGroup
                int loss = p.getTribe().applyShamanicLoss(prestigePointsLoss, ownerStars, maxStars, minStars);
                prestigeDeltas.put(p, prestigeDeltas.get(p) - loss);
            }
        }
        return new EventResult(prestigeDeltas, foodDeltas);
    }
}

