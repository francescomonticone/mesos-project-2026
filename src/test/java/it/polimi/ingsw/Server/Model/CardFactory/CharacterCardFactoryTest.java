package it.polimi.ingsw.Server.Model.CardFactory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CharacterCardFactoryTest {

    @Test
    void getAllCards() {
        System.out.println(new CharacterCardFactory().getAllCards());
    }
}