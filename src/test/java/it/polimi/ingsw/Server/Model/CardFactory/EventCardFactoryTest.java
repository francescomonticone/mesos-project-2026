package it.polimi.ingsw.Server.Model.CardFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventCardFactoryTest {

    @Test
    void load_test(){
        System.out.println(new EventCardFactory().getAllNonFinalCards());
    }

}