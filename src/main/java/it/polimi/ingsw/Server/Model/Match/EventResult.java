package it.polimi.ingsw.Server.Model.Match;
import java.util.*;

/**
 * Encapsulates the final outcome of an event for all players in the match.
 * <p>
 * This class acts as an immutable Value Object. It holds the calculated changes
 * (deltas) in Prestige Points and Food that need to be applied to each player's
 * board after an event (such as Sustenance, Hunt, or Shamanic Ritual) has been resolved.
 * </p>
 */
public class EventResult {
    private final Map<Player, Integer> prestigePointsDeltas;
    private final Map<Player,Integer> foodDeltas;

    /**
     * Constructs a new EventResult containing the exact resource changes for all players.
     * <p>
     * For safety, this constructor creates immutable defensive copies of the provided maps
     * and strictly validates that both maps track the exact same set of players.
     * </p>
     *
     * @param prestigePointsDeltas a map linking each player to their change in prestige points (can be negative, zero, or positive)
     * @param foodDeltas           a map linking each player to their change in food (can be negative, zero, or positive)
     * @throws IllegalArgumentException if the two maps do not contain the exact same set of {@link Player} keys
     */
    public EventResult(Map<Player, Integer> prestigePointsDeltas,
                       Map<Player, Integer> foodDeltas) {

        //check if the keySet of players is the same
        if (!prestigePointsDeltas.keySet().equals(foodDeltas.keySet())) {
            throw new IllegalArgumentException("prestigePointsDeltas and foodDeltas must contain the same set of players");
        }

        this.prestigePointsDeltas = Map.copyOf(prestigePointsDeltas); //immutable defensive copy
        this.foodDeltas = Map.copyOf(foodDeltas); //immutable defensive copy
    }

    public Map<Player, Integer> getPrestigePointsDeltas() {
        return prestigePointsDeltas;
    }

    public Map<Player, Integer> getFoodDeltas() {
        return foodDeltas;
    }
}
