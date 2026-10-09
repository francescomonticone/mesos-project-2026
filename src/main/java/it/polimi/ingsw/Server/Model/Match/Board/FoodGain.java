package it.polimi.ingsw.Server.Model.Match.Board;

import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * A concrete {@link TileMode} strategy representing an automatic food reward.
 * <p>
 * When a player places their totem on an {@link OfferTile} configured with this mode,
 * they immediately receive a fixed amount of food without needing to make any further choices
 * or interactive selections.
 * </p>
 */
public class FoodGain implements TileMode {
    private final int food;

    /**
     * Constructs a new FoodGain strategy with the specified food reward.
     *
     * @param food the amount of food to be granted to the player
     */
    public FoodGain(int food){
        this.food = food;
    }

    /**
     * Indicates that this tile mode resolves automatically.
     *
     * @return {@code true}, as gaining food is an instant effect requiring no player input
     */
    @Override
    public boolean isAutomatic() {
        return true;
    }

    /**
     * Applies the instant effect by directly adding the predetermined amount of food
     * to the player's current supply.
     *
     * @param player the {@link Player} receiving the food bonus
     */
    @Override
    public void applyAutomaticEffect(Player player) {
        player.addFood(food); //the player gains the food (Automatic tile mode)
    }
}
