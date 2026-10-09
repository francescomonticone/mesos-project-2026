package it.polimi.ingsw.Client.View.CardRegistry;

import it.polimi.ingsw.Client.View.CardRegistry.ClientCharacter.ClientHunterData;
import it.polimi.ingsw.Server.Model.Cards.CharacterCards.Hunter;
import it.polimi.ingsw.Server.Model.Match.Era;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardRegistryTest {

    @Test
    void getCard() {
        CardRegistry test = new CardRegistry();
        for(int i = 1; i < 84; i++){
            if(i < 10){
                System.out.println(test.getCard("CH_0"+i));
            }else {
                System.out.println(test.getCard("CH_"+i));
            }
        }

        for(int i = 1; i < 13; i++){
            if(i < 10){
                System.out.println(test.getCard("EV_0"+i));
            }else {
                System.out.println(test.getCard("EV_"+i));
            }
        }

    }
}