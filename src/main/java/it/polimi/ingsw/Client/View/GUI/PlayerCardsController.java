package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Network.DTO.PlayerDTO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for the player cards popup window ({@code player_cards.fxml}).
 *
 * <p>Shows the character and building cards owned by a specific player.</p>
 */
@SuppressWarnings("unused")
public class PlayerCardsController {

    @FXML private Label    playerNameLabel;
    @FXML private FlowPane characterCardsPane;
    @FXML private FlowPane buildingCardsPane;

    private Stage popupStage;

    /**
     * Called right after the FXML is loaded.
     *
     * @param player     the player whose cards are displayed
     * @param popupStage the Stage of this popup (used to close it)
     */
    public void init(PlayerDTO player, Stage popupStage) {
        this.popupStage = popupStage;

        playerNameLabel.setText(player.nickname() + "'s Cards");

        populateCards(characterCardsPane, player.ownedCharacterIds(), "#cedcd8");
        populateCards(buildingCardsPane,  player.ownedBuildingIds(),  "#e0ced7");
    }

    /**
     * Refreshes the popup contents using the latest player data.
     *
     * @param player the player whose cards should be displayed
     */
    public void refresh(PlayerDTO player) {
        populateCards(characterCardsPane, player.ownedCharacterIds(), "#cedcd8");
        populateCards(buildingCardsPane,  player.ownedBuildingIds(),  "#e0ced7");
    }

    // ── FXML handler ──────────────────────────────────────────────────────────
    /**
     * Closes the player cards popup window.
     */
    @FXML
    private void onCloseClicked() {
        popupStage.close();
    }

    // ── Private helper ────────────────────────────────────────────────────────

    /**
     * Builds a card tile for each card ID and adds it to the given pane.
     *
     * @param pane    the container to populate
     * @param cardIds list of card IDs to display
     * @param color   background color of each tile
     */
    private void populateCards(FlowPane pane, List<String> cardIds, String color) {
        pane.getChildren().clear();

        if (cardIds == null || cardIds.isEmpty()) {
            Label empty = new Label("None");
            empty.setStyle("-fx-text-fill: #7a7974; -fx-font-size: 11;");
            pane.getChildren().add(empty);
            return;
        }

        for (String cardId : cardIds) {
            VBox tile = new VBox();
            tile.setPrefWidth(130);
            tile.setPrefHeight(180);
            tile.setAlignment(javafx.geometry.Pos.CENTER);
            tile.setPadding(new Insets(4));
            tile.setStyle("-fx-background-color: " + color + "; "
                    + "-fx-background-radius: 6; "
                    + "-fx-border-color: #dcd9d5; -fx-border-radius: 6;");


            try {
                //retrieve the image
                Image img = CardImageResolver.resolve(cardId);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(120);
                iv.setFitHeight(170);
                iv.setPreserveRatio(true); //keep proportion of the image
                iv.setSmooth(true);  //best quality

                //tooltip for seeing the ID with the mouse
                Tooltip tip = new Tooltip(cardId);
                tip.setShowDelay(javafx.util.Duration.millis(300));
                Tooltip.install(tile, tip); //link the tooltip to the tile

                //add the image to the tile
                tile.getChildren().add(iv);

            } catch (Exception e) { // if the image can't be retrieved
                Label idLabel = new Label(cardId);
                idLabel.setWrapText(true);
                idLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #28251d; -fx-font-weight: bold;");
                idLabel.setAlignment(javafx.geometry.Pos.CENTER);
                tile.getChildren().add(idLabel);
            }

            pane.getChildren().add(tile);

        }
    }
}
