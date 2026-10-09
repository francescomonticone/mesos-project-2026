package it.polimi.ingsw.Server.Model.Match.GameState.Actions;

import it.polimi.ingsw.Server.Model.Match.GameState.GameAction;
import it.polimi.ingsw.Server.Model.Match.GameState.GameState;
import it.polimi.ingsw.Server.Model.Match.MatchModel;

import java.util.List;

public class ResolveOfferTileAction implements GameAction {

    private final String playerNickname;
    private final List<String> upperRowIds;  //upper row
    private final List<String> lowerRowIds;  //lower row
    private final List<String> OrderedIds;

    public ResolveOfferTileAction(String playerNickname,
                                  List<String> upperRowIds,
                                  List<String> lowerRowIds, List<String> OrderedIds) {
        this.playerNickname = playerNickname;
        this.upperRowIds = List.copyOf(upperRowIds);
        this.lowerRowIds = List.copyOf(lowerRowIds);
        this.OrderedIds = List.copyOf(OrderedIds);
    }

    public String getPlayerNickname() { return playerNickname; }

    public List<String> getUpperRowIds() { return upperRowIds; }
    public List<String> getLowerRowIds() { return lowerRowIds; }

    public List<String> getOrderedIds() { return OrderedIds; }

    @Override
    public GameState applyTo(GameState state, MatchModel model)
            throws IllegalActionException {
        return state.onResolveOfferTile(this, model);
    }
}