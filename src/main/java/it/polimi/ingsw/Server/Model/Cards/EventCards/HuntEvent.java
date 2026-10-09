package it.polimi.ingsw.Server.Model.Cards.EventCards;


import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.EventResult;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HuntEvent extends EventCard{
    private final int prestigePointsBonus;
    private final int huntFoodBonus; //this is for future HuntEvent more powerful cards

    public HuntEvent(String id, Era era, int minPlayers, boolean isFinalEvent, int prestigePointsBonus, int huntFoodBonus) {
        super(id, era, minPlayers, isFinalEvent);
        this.prestigePointsBonus = prestigePointsBonus;
        this.huntFoodBonus = huntFoodBonus;
    }

    /**
     * Calculates the deltas for food and prestige points for each player based on their tribe's hunting capabilities.
     * <p>
     * The method calculates the base outcome of the hunt. Any special effects from buildings
     * are expected to be processed separately via the BuildingEffect hooks in the game controller.
     * </p>
     *
     * @param players the list of {@link Player}s currently in the match
     * @return an {@link EventResult} containing the food and prestige points deltas for each player
     * @throws IllegalArgumentException if the provided players list is {@code null} or empty
     */

    @Override
    public EventResult calculate (List<Player> players){

        if (players == null || players.isEmpty())
            throw new IllegalArgumentException("Players list must not be null or empty");

        Map<Player, Integer> foodDeltas = new HashMap<>();
        Map<Player, Integer> prestigeDeltas = new HashMap<>();

        for (Player p : players) {
            Tribe t = p.getTribe();
            int totalFoodGained = huntFoodBonus * t.getTotalHunterCount();  //Hunt food multiplier (in the rules is 1, but we support future expansions) * total hunt counter considering other future expansions
            int totalPrestigePointsGained = this.prestigePointsBonus * t.getTotalHunterCount();
            int totalBuildingFoodGained = t.getTotalBuildingFoodOnHuntEvent();
            int totalBuildingPrestigePointsGained = t.getTotalBuildingPointsOnHuntEvent();
            foodDeltas.put(p, totalFoodGained + totalBuildingFoodGained);
            prestigeDeltas.put(p, totalPrestigePointsGained + totalBuildingPrestigePointsGained);
        }

        return new EventResult(prestigeDeltas, foodDeltas);
    }

}
