package it.polimi.ingsw.Server.Model.Match;

import it.polimi.ingsw.Server.Model.Cards.EventCards.EventCard;
import it.polimi.ingsw.Server.Model.Cards.EventCards.SustenanceEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventResolutionQueueTest {
    private EventResolutionQueue queue;

    @BeforeEach
    void setup(){
        queue = new EventResolutionQueue();
    }

    @Test
    @DisplayName("Default constructor test")
    void constructor(){
        assertNotNull(queue.getNormalEvents());
        assertNotNull(queue.getSustenanceEvents());
        assertTrue(queue.getNormalEvents().isEmpty());
        assertTrue(queue.getSustenanceEvents().isEmpty());
    }

    @Test
    @DisplayName("Standard addNormalEvent and addSustenanceEvent methods behaviour")
    void add(){
        // Setup
        EventCard e1 = mock(EventCard.class);
        EventCard e2 = mock(EventCard.class);
        EventCard e3 = mock(EventCard.class);

        SustenanceEvent s1 = mock(SustenanceEvent.class);
        SustenanceEvent s2 = mock(SustenanceEvent.class);
        SustenanceEvent s3 = mock(SustenanceEvent.class);

        when(e1.getEra()).thenReturn(Era.I);
        when(e2.getEra()).thenReturn(Era.II);
        when(e3.getEra()).thenReturn(Era.III);
        when(s1.getEra()).thenReturn(Era.I);
        when(s2.getEra()).thenReturn(Era.II);
        when(s3.getEra()).thenReturn(Era.III);

        // Simulate
        queue.addNormalEvent(e3);
        queue.addNormalEvent(e1);
        queue.addNormalEvent(e2);

        queue.addSustenanceEvent(s2);
        queue.addSustenanceEvent(s1);
        queue.addSustenanceEvent(s3);

        // Assert
        assertEquals(3, queue.getNormalEvents().size());
        assertEquals(3, queue.getSustenanceEvents().size());
        assertEquals(List.of(e1,e2,e3), queue.getNormalEvents()); // should order event based on Era
        assertEquals(List.of(s1,s2,s3), queue.getSustenanceEvents()); // should order event based on Era
    }
}