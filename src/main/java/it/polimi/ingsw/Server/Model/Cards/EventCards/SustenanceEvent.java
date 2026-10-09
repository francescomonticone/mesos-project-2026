package it.polimi.ingsw.Server.Model.Cards.EventCards;

import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Gatherer;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResolutionQueue;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Event card that represents a Sustenance phase: each player must feed
 * all characters in their {@link Tribe} by spending food tokens.
 *
 * <p>The total cost is calculated as {@code foodCost * totalCharacters}, reduced by any
 * discount provided by {@link Gatherer} characters. Players who cannot
 * fully pay lose an amount of prestige points proportional to {@code prestigePointsCost}.
 * </p>
 *
 * @author Francesco Monticone
 *
 * @see EventCard
 * @see EventResult
 */

public class SustenanceEvent extends EventCard{
    private final int prestigePointsCost;
    private final int foodCost;

    public SustenanceEvent(String id, Era era, int minPlayers, boolean isFinalEvent, int prestigePointsCost, int foodCost) {
        super(id, era, minPlayers, isFinalEvent);
        this.prestigePointsCost = prestigePointsCost;
        this.foodCost = foodCost;
    }

    @Override
    public void addCardToQueue(EventResolutionQueue queue) {
        queue.addSustenanceEvent(this); //this will add the sustenance card at the bottom of the queue
    }

    /**
     * Calculates food and prestige point deltas for each {@link Player}
     * during the Sustenance event, without modifying any game state.
     *
     * <p>Each player pays {@code foodCost} per character in their {@link Tribe},
     * reduced by their {@link Gatherer}s' discount. Players who cannot cover
     * the full cost spend all remaining food and lose {@code prestigePointsCost}
     * prestige points per unsatisfied character.
     * </p>
     * <p>
     * This calculation strictly covers the base characters' mechanics.
     * Building card side effects must be applied by the EventResolutionState.
     * </p>
     *
     * @param players the list of players participating in the event; must not be {@code null} or empty
     * @return an {@link EventResult} containing food and prestige point deltas for each player
     * @throws IllegalArgumentException if {@code players} is {@code null} or empty
     */

    @Override
    public EventResult calculate (List<Player> players){

        if (players == null || players.isEmpty())
            throw new IllegalArgumentException("Players list must not be null or empty");


        Map<Player, Integer> foodDeltas = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();

        for(Player p : players){
            Tribe t = p.getTribe();
            int tribeCost = t.getTotalCharactersCount() * foodCost;
            int totalSustenanceDiscount = t.getTotalSustenanceDiscount();
            int finalCost = Math.max(0, tribeCost - totalSustenanceDiscount); //compute final cost (always positive)

            int currentFood = p.getFoodToken();

            if(finalCost <= currentFood){ //food only payment
                foodDeltas.put(p, -finalCost);
                prestigeDeltas.put(p, 0);
            }
            else{ //not enough food
                foodDeltas.put(p, -currentFood);
                prestigeDeltas.put(p, -(finalCost-currentFood)*prestigePointsCost);
            }
        }
        return new EventResult(prestigeDeltas, foodDeltas);
    }
}
