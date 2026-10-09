package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * Represents a Hunter character card in the game.
 * <p>
 * Hunters specialize in generating food resources for a player's Tribe. They provide
 * a base food bonus that is typically evaluated during specific game events (such as the Hunt).
 * Furthermore, certain Hunter cards feature an immediate acquisition effect (denoted by a food icon),
 * granting an instant resource boost that scales synergistically with the total number of
 * Hunters already present in the tribe.
 * </p>
 */
public class Hunter extends CharacterCard{
    private final boolean hasFoodIcon;
    private final int foodBonus;

    public Hunter(int minPlayers, Era era, String id, boolean hasFoodIcon, int foodBonus, String type) {
        super(minPlayers, era, id, type);
        this.hasFoodIcon = hasFoodIcon;
        this.foodBonus = foodBonus;
    }

    @Override
    public int getFoodBonus() {
        return foodBonus;
    }

    @Override
    public String getType(){
        return type;
    }

    /**
     * Adds this hunter to the player's tribe and resolves any immediate acquisition effects.
     * <p>
     * It first delegates to the superclass to register the card and update the tribe's
     * class counters. Then, if this specific hunter features a food icon, it instantly
     * awards food to the player equal to its {@code foodBonus} multiplied by the total
     * number of hunters now present in the tribe.
     * </p>
     *
     * @param player the {@link Player} acquiring this hunter card
     */
    @Override
    public void addToTribe(Player player){
        super.addToTribe(player);
        if(hasFoodIcon)
            player.addFood(this.foodBonus*player.getTribe().getTotalHunterCount());
    }
}
