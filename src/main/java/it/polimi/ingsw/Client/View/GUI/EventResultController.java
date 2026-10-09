package it.polimi.ingsw.Client.View.GUI;

import it.polimi.ingsw.Network.DTO.EventResultDTO;
import it.polimi.ingsw.Network.DTO.PlayerDeltaDTO;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class EventResultController {
    private Stage popupStage;
    @FXML private VBox eventBox;

    /**
     * initializes and builds the event result scene
     *
     * @param results list of events with their results
     * @param popupStage window in which appears the event result
     */
    public void init(List<EventResultDTO> results, Stage popupStage){
        this.popupStage = popupStage;

        for(EventResultDTO result : results){
            HBox hbox = new HBox();
            hbox.setSpacing(20);
            hbox.setAlignment(javafx.geometry.Pos.CENTER);

            Image img = CardImageResolver.resolve(result.eventCardId());
            ImageView eventCard = new ImageView(img);
            eventCard.setFitWidth(170);
            eventCard.setFitHeight(240);

            Label prestigePointsLabel = new Label("Prestige points:");
            prestigePointsLabel.setStyle("-fx-text-fill: #501d0c;");
            Label foodLabel = new Label("Food:");
            foodLabel.setStyle("-fx-text-fill: #501d0c;");

            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.add(prestigePointsLabel, 0, 1); // column 0, row 1
            gridPane.add(foodLabel, 0, 2);
            gridPane.setAlignment(javafx.geometry.Pos.CENTER);
            GridPane.setHalignment(prestigePointsLabel, HPos.RIGHT);
            GridPane.setHalignment(foodLabel, HPos.RIGHT);

            Map<String, PlayerDeltaDTO> playerDeltas = result.playerDeltas();
            int i = 1; //start from second column
            for(String player : playerDeltas.keySet()){
                int prestigePointDelta = playerDeltas.get(player).pointsChange();
                int foodDelta = playerDeltas.get(player).foodChange();

                Label nameLabel = new Label(player);
                nameLabel.setStyle("-fx-text-fill: #501d0c;");
                Label foodDeltaLabel = new Label();
                Label prestigePointsDeltaLabel = new Label();

                labelFormat(prestigePointsDeltaLabel, prestigePointDelta);
                labelFormat(foodDeltaLabel, foodDelta);

                gridPane.add(nameLabel, i, 0);
                gridPane.add(prestigePointsDeltaLabel, i, 1);
                gridPane.add(foodDeltaLabel, i, 2);

                GridPane.setHalignment(prestigePointsDeltaLabel, HPos.CENTER);
                GridPane.setHalignment(foodDeltaLabel, HPos.CENTER);

                i++;
            }

            hbox.getChildren().addAll(eventCard, gridPane);
            eventBox.getChildren().add(hbox);
        }
    }

    /**
     * formats the label based on what value is delta
     *
     * @param label label that we want to format
     * @param delta the change in value
     */
    private void labelFormat(Label label, int delta){
        if(delta > 0){
            label.setText("↑" + delta);
            label.setStyle("-fx-text-fill: #141a7c;");
        }else if(delta < 0){
            delta = -delta;
            label.setText("↓" + delta);
            label.setStyle("-fx-text-fill: #ff0000;");
        } else {
            label.setText(String.valueOf(delta));
            label.setStyle("-fx-text-fill: #000000;");
        }
    }


    /**
     * closes the popup stage when the close button is clicked
     */
    @FXML
    private void onCloseClicked(){
        popupStage.close();
    }
}
