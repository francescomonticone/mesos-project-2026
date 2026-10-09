package it.polimi.ingsw.Client.View.TUI;

import it.polimi.ingsw.Client.View.CardRegistry.CardRegistry;
import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingData;
import it.polimi.ingsw.Client.View.CardRegistry.ClientBuilding.ClientBuildingEffect.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter.*;
import it.polimi.ingsw.Client.View.CardRegistry.ClientEvent.*;
import it.polimi.ingsw.Network.DTO.BoardDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

/**
 * Provides the Text-based User Interface (TUI) rendering logic for game cards and building effects.
 * This class implements the Visitor pattern to handle different card types, converting data objects into ASCII-based
 * representations.
 * <p>The renderer manages dimensions, borders, and spacing to ensure cards are aligned
 *  * correctly when printed side-by-side in the terminal.</p>
 *
 */

public class TuiCardRenderer implements ClientCardVisitor<List<String>>, ClientEffectVisitor<List<String>> {
    final int termWidth;
    final int height;
    final int width;
    final String border;
    final String separator;
    final String blankLine;
    final String spacing;
    final CardRegistry registry;
    final BoardDTO board;


    /**
     * Constructs a new TuiCardRenderer and initializes rendering constants.
     * Sets the default height, width, and border styles for the ASCII cards.
     *
     * @param match The current match data transfer object containing the board state.
     */
    public TuiCardRenderer(MatchDTO match, int termWidth){
        height = 14;
        width = 30;
        border = "+"+"-".repeat(width)+"+";
        separator = "|"+"-".repeat(width)+"|";
        blankLine = "|"+" ".repeat(width)+"|";
        spacing = "   ";
        registry = new CardRegistry();
        board = match.board();
        this.termWidth = termWidth;
    }


    /**
     * Transforms Hunter card data into a formatted list of ASCII strings.
     * Displays information regarding food bonuses and hunter-specific icons.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientHunterData card){
        List<String> lines = new ArrayList<>();
        int paddingTotal = width-card.type().length();
        int paddingLeft = paddingTotal/2;
        int paddingRight = paddingTotal-paddingLeft;

        lines.add(border);
        lines.add("|"+" ".repeat(paddingLeft)+card.type()+" ".repeat(paddingRight)+"|");
        lines.add(separator);
        if(card.hasFoodIcon()) {
            lines.add(String.format("| %-28s |", "Gain food: "+card.foodBonus() + " x HUNTER"));
        }else{
            lines.add(String.format("| %-28s |", "No food gain"));
        }
        int heightDiff = max(0, this.height - lines.size()-1);
        for(int i = 0; i < heightDiff; i++){
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Builder card data into a formatted list of ASCII strings.
     * Displays information regarding prestige points bonuses and building discount.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientBuilderData card) {
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - card.type().length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + card.type() + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Prestige points: "+card.prestigePoints()));
        lines.add(blankLine);
        lines.add(String.format("| %-28s |", "Building discount: "+card.buildingDiscount()));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Gatherer card data into a formatted list of ASCII strings.
     * Displays information regarding food discount bonuses.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientGathererData card) {
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - card.type().length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + card.type() + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Food discount: "+card.foodDiscount()));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Artists card data into a formatted list of ASCII strings.
     * Displays information regarding artist-specific icons.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientArtistData card) {
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - card.type().length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + card.type() + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Paintings bonus: "+card.artistBonus()));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Hunter card data into a formatted list of ASCII strings.
     * Displays information regarding inventor-specific icons.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientInventorData card) {
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - card.type().length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + card.type() + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Invention type: "+card.invention()));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Shaman card data into a formatted list of ASCII strings.
     * Displays information regarding shaman-specific icons.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientShamanData card) {
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - card.type().length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + card.type() + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Star bonus: "+ card.starPoint()));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }


    /**
     * Transforms Hunt event card data into a formatted list of ASCII strings.
     *
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientHuntEventData card){
        String name = "HUNT EVENT";
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Gain:"));
        lines.add(String.format("| - %-26s |", card.prestigePointsBonus()+" prestige pt. X HUNTER"));
        lines.add(String.format("| - %-26s |", card.huntFoodBonus()+" food X HUNTER"));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }


    /**
     * Transforms Sustenance event card data into a formatted list of ASCII strings.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientSustenanceEventData card){
        String name = "SUSTENANCE EVENT";
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Pay:"));
        lines.add(String.format("| - %-26s |", card.foodCost()+" food X CHARACTER"));
        lines.add(blankLine);
        lines.add(String.format("| %-28s |", "If you can't, pay: "));
        lines.add(String.format("| - %-26s |", card.prestigePointsCost()+" prestige pt. for"));
        lines.add(String.format("|   %-26s |", "every CHARACTER left"));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Shamanic ritual event card data into a formatted list of ASCII strings.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientShamanicRitualData card){
        String name = "SHAMANIC RITUAL EVENT";
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Player(s) with the most"));
        lines.add(String.format("| %-28s |", "stars gain:"));
        lines.add(String.format("| - %-26s |", card.prestigePointsGain()+" prestige pt."));
        lines.add(String.format("| %-28s |", "Player(s) with the least"));
        lines.add(String.format("| %-28s |", "stars lose:"));
        lines.add(String.format("| - %-26s |", card.prestigePointsLoss()+" prestige pt."));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Cave paintings card data into a formatted list of ASCII strings.
     *
     * @param card The data object representing the Hunter card.
     * @return A list of strings, where each string is a line of the formatted card.
     */
    @Override
    public List<String> visit(ClientCavePaintingsEventData card){
        String name = "CAVE PAINTINGS EVENT";
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Player(s) with:"));
        lines.add(String.format("| - %-26s |", "more than "+card.gainThreshold()+" ARTIST:"));
        lines.add(String.format("|   %-26s |", "gain "+card.prestigePointsPerArtist()+" prestige pt."));
        lines.add(String.format("| - %-26s |", "less than "+ card.lossThreshold()+" ARTIST:"));
        lines.add(String.format("|   %-26s |", "lose "+card.prestigePointsLost()+" prestige pt."));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Transforms Building card data into a formatted list of ASCII strings.
     * This method triggers the visitor for the building's effect to dynamically
     * render effect-specific descriptions within the card frame.
     *
     * @param card The data object representing the Building card.
     * @return A list of strings representing the building card and its nested effect.
     */
    @Override
    public List<String> visit(ClientBuildingData card){
        String name = "BUILDING";
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.add(String.format("| %-28s |", "Cost: "+card.foodCost()+" food"));
        if(card.finalPrestigePoints() != 0){
            lines.add(String.format("| %-28s |", "Final prestige pt.: +"+card.finalPrestigePoints()));
        }
        lines.add(blankLine);
        lines.add(String.format("| %-28s |", "EFFECT:"));
        lines.addAll(card.effect().accept(this));
        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 0; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }

    /**
     * Generates a text-based representation of the Food Per Complete Set effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientFoodPerCompleteSet effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "Gain "+effect.food()+" food for every"));
        lines.add(String.format("| %-28s |", "set of "+effect.setSize()+" CHARACTERS"));
        lines.add(String.format("| %-28s |", "(Sets formed prior the "));
        lines.add(String.format("| %-28s |", "acquisition are NOT counted)"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Extra Gatherer discount during the Sustenance event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientSustenanceGatherersDiscount effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SUSTENANCE EVENT,"));
        lines.add(String.format("| %-28s |", "pay "+effect.food()+" less food for "));
        lines.add(String.format("| %-28s |", "every GATHERER in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Extra Artist discount during the Sustenance event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientSustenanceArtistDiscount effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SUSTENANCE EVENT,"));
        lines.add(String.format("| %-28s |", "pay "+effect.food()+" less food for "));
        lines.add(String.format("| %-28s |", "every ARTIST in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Shield during Shamanic ritual event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientShamanicShield effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SHAMANIC RITUAL "));
        lines.add(String.format("| %-28s |", "EVENT, do not lose prestige"));
        lines.add(String.format("| %-28s |", "pt. if you have less stars"));
        lines.add(String.format("| %-28s |", "than other player(s)"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Extra food at turn end effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraFoodTurnEnd effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At TURN END, if the "));
        lines.add(String.format("| %-28s |", "player would have gained"));
        lines.add(String.format("| %-28s |", "food, gain "+effect.food()+" more food"));

        return lines;
    }


    /**
     * Generates a text-based representation of the Same inventor pair bonus effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientSameInventorPairBonus effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "Gain "+effect.food()+" food for every"));
        lines.add(String.format("| %-28s |", "pair of INVENTORS with "));
        lines.add(String.format("| %-28s |", "the same INVENTION"));
        lines.add(String.format("| %-28s |", "(Pairs formed prior the "));
        lines.add(String.format("| %-28s |", "acquisition are NOT counted)"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Double points gained during Shamanic ritual event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientDoubleShamanicPoints effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SHAMANIC RITUAL "));
        lines.add(String.format("| %-28s |", "EVENT, gain double the"));
        lines.add(String.format("| %-28s |", "amount of the prestige pt."));
        lines.add(String.format("| %-28s |", "the player would have"));
        lines.add(String.format("| %-28s |", "gained"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Extra shamanic stars during Shamanic ritual event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraShamanicStar effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SHAMANIC RITUAL "));
        lines.add(String.format("| %-28s |", "EVENT, gain "+effect.stars()+" more stars"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Extra Inventors discount during the Sustenance event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientSustenanceInventorsDiscount effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During SUSTENANCE EVENT,"));
        lines.add(String.format("| %-28s |", "pay "+effect.food()+" less food for "));
        lines.add(String.format("| %-28s |", "every INVENTOR in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus during Hunt event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientHuntEventBonus effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During HUNT EVENT,"));
        lines.add(String.format("| %-28s |", "gain "+effect.food()+" more food and"));
        lines.add(String.format("| %-28s |", effect.points()+" more prestige pt. for"));
        lines.add(String.format("| %-28s |", "every HUNTER in the tribe"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Double points gained from Builders effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientDoubleBuilderPoints effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain double"));
        lines.add(String.format("| %-28s |", "the amount of prestige pt."));
        lines.add(String.format("| %-28s |", "from BUILDER characters"));
        return lines;
    }


    /**
     * Generates a text-based representation of the Bonus during Cave paintings event effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientCavePaintingBonus effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "During CAVE PAINTINGS"));
        lines.add(String.format("| %-28s |", "EVENT, gain "+effect.foodPerArtist()+" food for "));
        lines.add(String.format("| %-28s |", "every ARTIST in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Extra prestige points per complete set effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientPointsPerCompleteSet effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "set of "+effect.setSize()+" CHARACTERS"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Artist effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerArtist effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "ARTIST in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Hunter effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerHunter effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "HUNTER in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Gatherer effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerGatherer effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "GATHERER in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Shaman effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerShaman effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "SHAMAN in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Builder effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerBuilder effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "BUILDER in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points per Inventor effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraPointPerInventor effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "prestige pt. for every"));
        lines.add(String.format("| %-28s |", "INVENTOR in the Tribe"));
        return lines;
    }

    /**
     * Generates a text-based representation of the Extra card pick effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientExtraCardPick effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "After all players picked"));
        lines.add(String.format("| %-28s |", "their cards, pick: "));
        if(effect.extraUpCardPick() != 0) {
            lines.add(String.format("| %-28s |", "- "+effect.extraUpCardPick()+" more card from"));
            lines.add(String.format("| %-28s |", "  the upper row"));
        }if(effect.extraDownCardPick() != 0) {
            lines.add(String.format("| %-28s |", "- "+effect.extraDownCardPick()+" more card from"));
            lines.add(String.format("| %-28s |", "  the upper row"));
        }
        return lines;
    }

    /**
     * Generates a text-based representation of the Bonus prestige points at the end of the game effect.
     *
     * @param effect The specific building effect data.
     * @return A list of strings describing the effect's mechanics.
     */
    @Override
    public List<String> visit(ClientBonusPoints effect){
        List<String> lines = new ArrayList<>();
        lines.add(String.format("| %-28s |", "At GAME END, gain "+effect.points()));
        lines.add(String.format("| %-28s |", "more prestige pts."));
        return lines;
    }

    /**
     * Renders the upper row of the board to the standard output.
     * Fetches cards from the registry based on current board IDs and prints them
     * horizontally. Each card is accompanied by its selection index centered below.
     */
    public void printUpperRow(){
        List<List<String>> cardsToPrint = new ArrayList<>();

        for(String id: board.upperRowCardIds()){
            registry.getCard(id).ifPresentOrElse(cardData ->
                    cardsToPrint.add(cardData.accept(this)), () ->  //add to the toPrint List
                    System.out.println("[ERROR] Failed to render card: " + id));
        }

        int totalCardWidth = width + 2 + spacing.length();  // 30 + 2 borders + 3 spaces = 35
        int maxPerRow = Math.max(1, termWidth / totalCardWidth);  // es. 80/35 = 2 card per row
        int cardNum = cardsToPrint.size();

        for (int groupStart = 0; groupStart < cardsToPrint.size(); groupStart += maxPerRow) {// prints only maxPerRow card each time, then it goes to the newline

            int groupEnd = Math.min(groupStart+maxPerRow, cardNum);

            for (int row = 0; row < height; row++) {
                for (int card = groupStart; card < groupEnd; card++) {
                    System.out.print(cardsToPrint.get(card).get(row));
                    System.out.print(spacing);
                }
                System.out.println();
            }

            for (int i = groupStart; i < groupEnd; i++) {
                String idx = "[" + board.upperRowCardIds().get(i) + "]";

                int paddingTotal = width + 2 - idx.length();
                int paddingLeft = paddingTotal / 2;
                int paddingRight = paddingTotal - paddingLeft;

                System.out.print(" ".repeat(paddingLeft) + idx + " ".repeat(paddingRight) + spacing);
            }
            System.out.println();
        }

    }

    /**
     * Renders the lower row of the board to the standard output.
     * Fetches cards from the registry based on current board IDs and prints them
     * horizontally. Each card is accompanied by its selection index centered below.
     */
    public void printLowerRow(){
        List<List<String>> cardsToPrint = new ArrayList<>();

        for(String id: board.lowerRowCardIds()){
            registry.getCard(id).ifPresentOrElse(cardData ->
                    cardsToPrint.add(cardData.accept(this)), () ->  //add to the toPrint List
                    System.out.println("[ERROR] Failed to render card: " + id));
        }

        int totalCardWidth = width + 2 + spacing.length();  // 30 + 2 borders + 3 spaces = 35
        int maxPerRow = Math.max(1, termWidth / totalCardWidth);  // es. 80/35 = 2 card per row
        int cardNum = cardsToPrint.size();


        for (int groupStart = 0; groupStart < cardNum; groupStart += maxPerRow) { // prints only maxPerRow card each time, then it goes to the newline

            int groupEnd = Math.min(groupStart+maxPerRow, cardNum);

            for (int row = 0; row < height; row++) {
                for (int card = groupStart; card < groupEnd; card++) {
                    System.out.print(cardsToPrint.get(card).get(row));
                    System.out.print(spacing);
                }
                System.out.println();
            }
            for (int i = groupStart; i < groupEnd; i++) {
                String idx = "[" + board.lowerRowCardIds().get(i) + "]";
                int paddingTotal = width + 2 - idx.length();
                int paddingLeft = paddingTotal / 2;
                int paddingRight = paddingTotal - paddingLeft;
                System.out.print(" ".repeat(paddingLeft) + idx + " ".repeat(paddingRight) + spacing);
            }
            System.out.println();
        }
    }


    /**
     * Renders an arbitrary list of card IDs using the same style as board rows.
     * Used to display tribe cards (characters and buildings) in the player's area, allowing for dynamic rendering of any card list with proper formatting and spacing.
     *
     * @param ids the list of card IDs to render
     */
    public void printCardRow(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;

        List<List<String>> cardsToPrint = new ArrayList<>();

        for (String id : ids) { //take cards from ID
            registry.getCard(id).ifPresentOrElse(
                    cardData -> cardsToPrint.add(cardData.accept(this)),
                    () -> System.out.println("[ERROR] Failed to render card: " + id)
            );
        }

        int totalCardWidth = width + 2 + spacing.length();  // 30 + 2 borders + 3 spaces = 35
        int maxPerRow = Math.max(1, termWidth / totalCardWidth);  // es. 80/35 = 2 card for row
        int cardNum = cardsToPrint.size();

        for (int groupStart = 0; groupStart < cardNum; groupStart += maxPerRow) { // prints only maxPerRow card each time, then it goes to the newline
            int groupEnd = Math.min(groupStart+maxPerRow, cardNum);

            for (int row = 0; row < height; row++) {
                for (int card = groupStart; card < groupEnd; card++) {
                    System.out.print(cardsToPrint.get(card).get(row));
                    System.out.print(spacing);
                }
                System.out.println();
            }
            for (int i = groupStart; i < groupEnd; i++) {
                String idx = "[" + ids.get(i) + "]";
                int paddingTotal = width + 2 - idx.length();
                int paddingLeft = paddingTotal / 2;
                int paddingRight = paddingTotal - paddingLeft;
                System.out.print(" ".repeat(paddingLeft) + idx + " ".repeat(paddingRight) + spacing);
            }
            System.out.println();
        }
    }
}
