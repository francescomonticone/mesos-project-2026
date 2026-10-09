package it.polimi.ingsw.Client.View.TUI;

import it.polimi.ingsw.Client.View.ClientBoard.ClientCardPick;
import it.polimi.ingsw.Client.View.ClientBoard.ClientFoodGain;
import it.polimi.ingsw.Client.View.ClientBoard.ClientTileVisitor;
import it.polimi.ingsw.Client.View.ClientBoard.BoardRegistry;
import it.polimi.ingsw.Network.DTO.BoardDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.OfferTileDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;

 /**
 * Handles the graphical TUI representation of the game's Offer Track.
 * This class implements the {@link ClientTileVisitor} interface to dynamically render
 * different tile actions (food gain vs. card picking) within an ASCII frame.
 * * <p>It manages the horizontal alignment of tiles and displays real-time
 * information about player occupants.</p>
 */
public class TuiBoardRenderer implements ClientTileVisitor<List<String>> {
    final int termWidth;
    final int height;
    final int width;
    final String border;
    final String separator;
    final String blankLine;
    final BoardRegistry registry;
    final BoardDTO board;
    final int playerNum;

     /**
      * Constructs the renderer and initializes layout constants and the tile registry.
      *
      * @param match     The current match DTO containing the board state and track information.
      * @param termWidth The maximum width of the terminal, used to wrap elements on multiple lines.
      */
    public TuiBoardRenderer(MatchDTO match, int termWidth){
        height = 11;
        width = 28;
        border = "+"+"-".repeat(width)+"+";
        separator = "|"+"-".repeat(width)+"|";
        blankLine = "|"+" ".repeat(width)+"|";
        registry = new BoardRegistry(match);
        board = match.board();
        playerNum = match.players().size();
        this.termWidth = termWidth;
    }

     /**
     * Creates the ASCII visual representation for a single offer tile.
     * It combines tile data from the registry with dynamic state information
     * such as the current occupant's nickname.
     *
     * @param tile The DTO representing the tile's current state on the board.
     * @return A list of strings representing the formatted lines of the tile.
     */
    public List<String> addTile(OfferTileDTO tile){
        String name = "TILE "+ tile.tileId();
        List<String> lines = new ArrayList<>();
        int paddingTotal = width - name.length();
        int paddingLeft = paddingTotal / 2;
        int paddingRight = paddingTotal - paddingLeft;

        lines.add(border);
        lines.add("|" + " ".repeat(paddingLeft) + name + " ".repeat(paddingRight) + "|");
        lines.add(separator);
        lines.addAll(registry.getOfferTrack().get(tile.tileId()).tileMode().accept(this));

        int heightDiff = max(0, this.height - lines.size() - 1);
        for (int i = 1; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        if(!(Objects.isNull(tile.occupantNickname()) || tile.occupantNickname().isEmpty())){
            name = "["+tile.occupantNickname()+"]";
        }else{
            name = "[]";
        }
        paddingTotal = width-name.length();
        paddingLeft = paddingTotal/2;
        paddingRight = paddingTotal - paddingLeft;
        lines.add("|"+ " ".repeat(paddingLeft)+name+" ".repeat(paddingRight)+"|");
        lines.add(border);
        return lines;
    }

     /**
     * Renders the specific lines for a tile that provides food gain.
     *
     * @param action The food gain action data.
     * @return A list of strings describing the food bonus.
     */
    @Override
    public List<String> visit(ClientFoodGain action){
        return List.of(blankLine, String.format("| %-26s |", "Gain: "+action.food()+" food"));
    }

     /**
     * Renders the specific lines for a tile that allows picking cards from the board.
     * Lists the number of picks available for the upper and lower rows.
     *
     * @param action The card pick action data.
     * @return A list of strings describing the picking rules.
     */
    @Override
    public List<String> visit(ClientCardPick action){
        List<String> lines = new ArrayList<>();

        lines.add(String.format("| %-26s |", "Pick: "));
        if(action.upperRowCount() > 0) {
            lines.add(String.format("| %-26s |", "- " + action.upperRowCount() + " card from upper row"));
        }
        if(action.lowerRowCount() > 0) {
            lines.add(String.format("| %-26s |", "- " + action.lowerRowCount() + " card from lower row"));
        }

        return lines;
    }

      /**
      * Prints the entire Offer Track to the standard output.
      * The tiles are printed horizontally side-by-side, with their respective
      * identification indices centered below each tile.
      */
    public void printOfferTrack(){
        List<List<String>> toPrint = new ArrayList<>();
        String spacing = "   ";

        toPrint.add(addTurnOrderTile());


        for(OfferTileDTO tile: board.offerTrackDTO()){
            toPrint.add(addTile(tile));
        }

        int totalTileWidth = width + 2 + spacing.length();
        int maxPerRow = Math.max(1, termWidth/totalTileWidth);
        int tileNum = toPrint.size();

        for(int groupStart = 0; groupStart < tileNum; groupStart += maxPerRow){

            int groupEnd = Math.min(groupStart+maxPerRow, tileNum);

            for (int row = 0; row < height; row++) {
                for (int tile = groupStart; tile < groupEnd; tile++) {
                    System.out.print(toPrint.get(tile).get(row));
                    System.out.print(spacing);
                }
                System.out.println();
            }
        }

    }

     /**
      * Creates the ASCII visual representation for the Turn Order tile.
      * <p>
      * It displays the current queue of players alongside their respective
      * placement bonuses (e.g., extra food) or penalties (e.g., lost food and prestige points).
      * </p>
      *
      * @return A list of strings representing the formatted lines of the Turn Order tile.
      */
    private List<String> addTurnOrderTile(){
        List<String> lines = new ArrayList<>();
        List<Integer> bonus = registry.getOrderFoodBonus();
        List<String> playerOrder = board.turnOrderTileDTO().playerTopToBottom();

        lines.add(border);
        lines.add(blankLine);

        for(int i = 0; i < playerNum-1; i++){
            if(!(Objects.isNull(playerOrder.get(i)))) {
                lines.add(String.format("| %-26s |", "[" + playerOrder.get(i) + "]: +" + bonus.get(i) + " food"));
            }else{
                lines.add(String.format("| %-26s |", "[]: +" + bonus.get(i) + " food"));
            }
        }

        if(!(Objects.isNull(playerOrder.getLast()))) {
            lines.add(String.format("| %-26s |", "[" + playerOrder.getLast() + "]: -1 food"));
            lines.add(String.format("| %-26s |", " ".repeat(playerOrder.getLast().length())+"   (-2 pp)"));
        }else{
            lines.add(String.format("| %-26s |", "[]: -1 food"));
            lines.add(String.format("| %-26s |", "    (-2 pp)"));
        }

        int heightDiff = max(0, this.height - lines.size());
        for (int i = 1; i < heightDiff; i++) {
            lines.add(blankLine);
        }
        lines.add(border);
        return lines;
    }
}
