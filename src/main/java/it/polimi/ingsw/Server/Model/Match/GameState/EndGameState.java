package it.polimi.ingsw.Server.Model.Match.GameState;

import it.polimi.ingsw.Network.DTO.MatchResultDTO;
import it.polimi.ingsw.Server.DAO.ResultDAO;
import it.polimi.ingsw.Server.Model.Match.MatchModel;
import it.polimi.ingsw.Server.Model.Match.Player;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Terminal state representing the end of a Mesos match.
 *
 * <p>This automatic state handles the final scoring phase. It triggers the
 * computation of final prestige points for all players, resolves tie-breakers
 * according to the official rules (typically remaining food), and updates
 * the match model with the final absolute winner.</p>
 *
 * <p>Returning {@link Optional#empty()} from this state signals to the
 * Game Controller that the state machine has officially halted and the
 * match is over.</p>
 */
public class EndGameState implements GameState {

    /**
     * Executes the final scoring phase and stops the game loop.
     *
     * @param model the current match model
     * @return {@link Optional#empty()} as this is the terminal state
     */
    @Override
    public Optional<GameState> onEnter(MatchModel model) {

        List<Player> players = model.getPlayers();

        // trigger final score calculations for each player.
        // also consider final buildings
        for (Player p : players) {
            p.computeAndApplyFinalPrestige(); //this method will add all the final PP directly in the player's tribe
        }

        // determine the max prestige score (winner)
        int maxPrestige = players.stream()
                .mapToInt(Player::getPrestigePoint)
                .max()
                .orElse(0);

        // filter players who achieved the highest score
        List<Player> topScorers = players.stream()
                .filter(p -> p.getPrestigePoint() == maxPrestige)
                .collect(Collectors.toList());

        // resolve tie-case (Rule: Most leftover food wins)
        // If there's a tie, we find the max food among the top scorers.
        List<Player> winners;

        if (topScorers.size() > 1) {
            int maxFood = topScorers.stream()
                    .mapToInt(Player::getFoodToken) // Use your actual getter method
                    .max()
                    .orElse(0);

            // filter down to the players who have both max prestige AND max food
            winners = topScorers.stream()
                    .filter(p -> p.getFoodToken() == maxFood)
                    .collect(Collectors.toList());
        } else {
            winners = topScorers;
        }

        // update the Model
        model.setWinners(winners);
        model.setMatchFinished(true);

        // prepare the leaderboard
        // order players by PP points
        List<Player> matchRanking = new java.util.ArrayList<>(players);

        //sort with a comparator, passed as a lambda function. inverted parameters ==> descending order
        matchRanking.sort((p1, p2) -> {   //sort by prestige points first, then by food tokens for tie-breaker

            if (p1.isConnected() != p2.isConnected()) { //ONLY SOME PLAYERS CONNECTED AT THE END OF THE GAME
                return p1.isConnected() ? -1 : 1; // Connected (true) comes first
            }

            //if both are connected
            if (p1.getPrestigePoint() != p2.getPrestigePoint()) {
                return Integer.compare(p2.getPrestigePoint(), p1.getPrestigePoint()); //value > 0 if p2.getPrestigePoint() > p1.getPrestigePoint()
            }
            return Integer.compare(p2.getFoodToken(), p1.getFoodToken());
        });

        // convert in DTO for network
        List<MatchResultDTO> matchRankingDTO = matchRanking.stream()
                .map(p -> new MatchResultDTO(p.getNickname() + (p.isConnected() ? "" : " (Disconnected)"), p.getPrestigePoint(), p.getFoodToken(), new java.util.Date()))
                .toList();

        model.setFinalMatchRanking(matchRankingDTO);


        int numPlayers = players.size();
        
        ResultDAO.insertResult(matchRanking, model.getMatchId());

        // retrieve the global leaderboard from the DB as a list of DTO
        List<MatchResultDTO> globalLeaderboardDTO = ResultDAO.getLeaderboard(numPlayers);

        model.setGlobalLeaderboard(globalLeaderboardDTO);

        // stop the state machine
        return Optional.empty();
    }
}