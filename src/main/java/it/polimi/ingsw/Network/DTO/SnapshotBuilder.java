package it.polimi.ingsw.Network.DTO;

import it.polimi.ingsw.Server.Model.Cards.Card;
import it.polimi.ingsw.Server.Model.Match.Board.Board;
import it.polimi.ingsw.Server.Model.Match.Board.OfferTile;
import it.polimi.ingsw.Server.Model.Match.Board.TurnOrderTile;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;
import it.polimi.ingsw.Server.Model.Match.Tribe;

import java.util.List;

/**
 * Utility class responsible for converting the complex domain model (MatchModel)
 * into lightweight, immutable Data Transfer Objects (DTOs) for network transmission.
 * <p>
 * This class ensures that sensitive game logic and references are not sent to the clients,
 * preserving security (anti-cheating) and reducing network overhead.
 * </p>
 * <p>
 * NOTE: Following the "Extreme Command Pattern" architecture, this builder NO LONGER
 * extracts the current game state name. The game phase is handled polymorphically
 * by the specific {@code ServerToClientMessage} command that wraps this DTO.
 * </p>
 */
public final class SnapshotBuilder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * Throws an exception if reflection is used to bypass it.
     */
    private SnapshotBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Builds the root snapshot of the entire match.
     * This is the only public method used to generate the DTO.
     *
     * @param model the current state of the game model
     * @return a {@link MatchDTO} representing the public state of the game
     */
    public static MatchDTO build(MatchModel model) {

        // create the BoardDTO
        BoardDTO boardDTO = buildBoard(model.getBoard());

        // create the list of PlayerDTOs
        List<PlayerDTO> playersDTO = model.getPlayers().stream()
                .map(SnapshotBuilder::buildPlayer)
                .toList();

        //send the player that has to play, if the state is interactive. If it's an automatic state, this will be null and the client will know to wait for the next update.
        String currentPlayerNickname = model.getCurrentState().getCurrentPlayer()
                .map(Player::getNickname)
                .orElse(null); //if there is an error and the state is automatic

        //get interactive states names to view in the TUI/GUI with the correct game state
        String phaseName = model.getCurrentState().getPhaseName();

        int remainingTime = 0;
        long deadline = model.getSuspensionDeadline();

        if (deadline > 0) { //if there is a deadline set compute the remaining time
            long millisLeft = deadline - System.currentTimeMillis();
            remainingTime = (int) (millisLeft / 1000); //remaining time in seconds sent to the network
            if (remainingTime < 0) remainingTime = 0; //avoid negative time due to thread latency
        }

        List<EventResultDTO> latestEvents = model.getCurrentRoundEventResults(); //get the EventResults list and build the DTOs
        model.clearEventResults(); //to avoid reading old event results

        // assemble the final MatchDTO
        return  new MatchDTO(
                model.getMatchId(),
                model.getCurrentRound(),
                model.getCurrentEra().getValue(),
                currentPlayerNickname,
                boardDTO,
                playersDTO,
                phaseName,
                model.getTotalDisconnectionTime(),
                remainingTime,
                latestEvents
        );
    }

    /**
     * Extracts the public state of a single player.
     *
     * @param player the player to extract data from
     * @return a {@link PlayerDTO} with the player's resources and collected card IDs
     */
    private static PlayerDTO buildPlayer(Player player) {
        // extract only the IDs of the owned characters to avoid sending full Card objects
        List<String> characterIds = player.getTribe().getCharacterCardList().stream()
                .map(Card::getId)
                .toList();

        // extract only the IDs of the owned buildings to avoid sending full Card objects
        List<String> buildingIds = player.getTribe().getBuildingCardList().stream()
                .map(Card::getId)
                .toList();

        return new PlayerDTO(
                player.getNickname(),
                player.getFoodToken(),
                player.getPrestigePoint(),
                player.getTotem() != null ? player.getTotem().name() : "NONE",
                player.isConnected(),
                characterIds,
                buildingIds,
                buildTribeDTO(player.getTribe()) //build the TribeDTO for this player
        );
    }

    /**
     * Builds a DTO snapshot of the given tribe using its current aggregate counts.
     *
     * @param tribe the tribe to convert into a {@code TribeDTO}
     * @return a {@code TribeDTO} containing all computed tribe statistics
     */
    public static TribeDTO buildTribeDTO(Tribe tribe) {

        int hunters = tribe.getTotalHunterCount();
        int shamans = tribe.getTotalShamanCount();
        int shamanStars = tribe.getShamanStarsCount(); //get the number of shaman stars from the tribe
        int artists = tribe.getTotalArtistCount();
        int builders = tribe.getTotalBuilderCount();
        int discount = tribe.getBuildingDiscount();
        int gatherers = tribe.getTotalGathererCount();
        int inventors = tribe.getTotalInventorCount();
        int distinctInvention = tribe.getDistinctInventionsCount();
        int inventionPair = tribe.getEqualInventionsCoupleCount();
        int sustenanceDiscount = tribe.getTotalSustenanceDiscount();

        return new TribeDTO(
                hunters,
                shamans,
                shamanStars,
                artists,
                builders,
                discount,
                gatherers,
                inventors,
                distinctInvention,
                inventionPair,
                sustenanceDiscount
        );
    }

    /**
     * Extracts the public state of the game board.
     *
     * @param board the board to extract data from
     * @return a {@link BoardDTO} with available cards and track statuses
     */
    private static BoardDTO buildBoard(Board board) {
        // map upper row cards to their string IDs
        List<String> upperIds = board.getUpperRow().stream()
                .map(Card::getId)
                .toList();

        // map lower row cards to their string IDs
        List<String> lowerIds = board.getLowerRow().stream()
                .map(Card::getId)
                .toList();

        // map the offer track tiles to OfferTileDTOs
        List<OfferTileDTO> offerTrackDTO = board.getOfferTrack().getTiles().stream()
                .map(SnapshotBuilder::buildOfferTile)
                .toList();

        // map the turn order tile
        TurnOrderTileDTO turnOrderDTO = buildTurnOrderTile(board.getTurnOrderTile());

        return new BoardDTO(
                upperIds,
                lowerIds,
                offerTrackDTO,
                turnOrderDTO
        );
    }

    /**
     * Extracts the public state of a single Offer Tile.
     *
     * @param tile the offer tile to analyze
     * @return an {@link OfferTileDTO} indicating the tile ID and its occupant
     */
    private static OfferTileDTO buildOfferTile(OfferTile tile) {
        String occupantName = !tile.isFree() ? tile.getOccupant().getNickname() : null;
        return new OfferTileDTO(tile.getTileId(), occupantName);
    }

    /**
     * Extracts the public state of the Turn Order Tile.
     *
     * @param turnOrderTile the turn order tile to analyze
     * @return a {@link TurnOrderTileDTO} with the ordered list of occupants
     */
    private static TurnOrderTileDTO buildTurnOrderTile(TurnOrderTile turnOrderTile) {
        List<String> occupants = turnOrderTile.getPlayerOrderTopBottom().stream()
                .map(player -> player != null ? player.getNickname() : null)
                .toList();

        return new TurnOrderTileDTO(occupants);
    }
}