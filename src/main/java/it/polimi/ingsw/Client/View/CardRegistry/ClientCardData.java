package it.polimi.ingsw.Client.View.CardRegistry;

import it.polimi.ingsw.Client.View.TUI.ClientCardVisitor;
import it.polimi.ingsw.Server.Model.Match.Era;

 /**
 * Base interface for all client-side card representations.
 * Defines common attributes and the entry point for the Visitor pattern.
  *
  * @author Gabriele Maiolo
 */
public interface ClientCardData {


    String id();
    Era era();
    int minPlayers();

     /**
      * Entry point for the Visitor pattern.
      * Accepts a visitor for polymorphic processing of card data.
      *
      * @param visitor The visitor implementation.
      * @return The result of the visit operation.*
      */
    <T> T accept(ClientCardVisitor<T> visitor);
}
