package it.polimi.ingsw.Server.Model.Cards.CharacterCards;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Era;
import it.polimi.ingsw.Server.Model.Match.Player;

/**
 * The abstract base class for all Character cards in the game.
 * <p>
 * Character cards represent the members of a player's Tribe (e.g., Hunters, Gatherers, Builders).
 * This class provides default implementations for all specific character abilities, returning
 * {@code 0} or {@code null}. This design leverages polymorphism, allowing the
 * {@link it.polimi.ingsw.Server.Model.Match.Tribe} class to iterate through all characters
 * and sum up their bonuses without needing to check their specific subclass types or perform casting.
 * </p>
 */
public abstract class CharacterCard extends Card {
    protected final String type;
    public CharacterCard(int minPlayers, Era era, String id, String type) {
        super(id, era, minPlayers);
        this.type = type;
    }

    /**
     * Returns the food discount this character provides during the Sustenance event.
     * Defaults to {@code 0}; subclasses may override this to support cards with
     * a food discount power, including future expansions.
     *
     * @return the food discount value, or {@code 0} if this character provides none
     */

    public int getFoodDiscount() {
        return 0;
    }
    
    public int getFoodBonus(){
        return 0;
    }

    public int getArtistBonus(){return 0;}

    public int getShamanStars() {
        return 0;
    }

    public InventionIcon getInvention() {
        return null;
    }

    public int getBuilderPoints(){
        return 0;
    }

    public int getBuildingDiscount(){return 0;}

    public abstract String getType(); //subclasses have to override this method, it is for the complete set runtime counter

    @Override
    public boolean isPickable() {
        return true;
    }

    /**
     * Adds this character card to the player's tribe.
     * <p>
     * It updates the tribe's internal occurrence counter (for set-collection mechanics)
     * and physically adds the card to the player's active collection.
     * </p>
     *
     * @param player the {@link Player} whose tribe is receiving the card
     */
    public void addToTribe(Player player){
        player.getTribe().updateCounter(type);
        player.addCharacterToTribe(this);
    }

    /**
     * Places the character card on the board during the initial setup phase.
     * <p>
     * According to the game rules, character cards drawn during the initial setup
     * (Round 0) are placed exclusively in the lower row to form the starting offer.
     * </p>
     *
     * @param board the main game {@link Board}
     */
    @Override
    public void placeOnBoardDuringSetup(Board board) {
        board.addCardToLowerRow(this);
    }
}
