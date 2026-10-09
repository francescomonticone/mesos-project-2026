package it.polimi.ingsw.Client.View.TUI;

import it.polimi.ingsw.Client.View.CardRegistry.CardRegistry;
import it.polimi.ingsw.Network.DTO.BoardDTO;
import it.polimi.ingsw.Network.DTO.MatchDTO;
import it.polimi.ingsw.Network.DTO.TurnOrderTileDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TuiCardRendererTest {

    TurnOrderTileDTO t = mock(TurnOrderTileDTO.class);

    @Test
    void print(){
        TuiCardRenderer renderer = new TuiCardRenderer(new MatchDTO(0,0,1, null,  new BoardDTO(List.of("BL_01","BL_03"), List.of("CH_01", "EV_10"), List.of(), t), List.of(), null, 0, 0, null), 160);
        renderer.printUpperRow();
        System.out.println();
        renderer.printLowerRow();
    }

}