package controllers;

import events.CreationProcessEvent;
import events.SwitchSceneEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import models.ChunkManager;
import views.CreationCellFactory;
import models.Creation;
import models.CreationManager;
import models.MediaSingleton;

import java.io.IOException;
import java.util.Comparator;

public class AdaptivePanel extends Controller {

    @FXML
    BorderPane adaptiveArea;
    @FXML
    ComboBox<Comparator<Creation>> sortDropdown;

    @FXML
    ListView<Creation> creationsListView;

    @FXML
    Button createButton;
    @FXML
    Button deleteButton;

    private SortedList<Creation> sortedCreations; // Could be local variable in initialise()?

    @FXML public void initialize() throws IOException {
        loadScene("/WelcomeView.fxml");

        CreationManager.getInstance().load();
        sortedCreations = CreationManager.getInstance().getItems().sorted();

        sortDropdown.setItems(CreationManager.getComparators());
        sortDropdown.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Comparator<Creation>>() {
            @Override
            public void changed(ObservableValue<? extends Comparator<Creation>> observable, Comparator<Creation> oldValue, Comparator<Creation> newValue) {
                if (newValue != null) {
                    sortedCreations.setComparator(newValue);
                }
            }
        });
        sortDropdown.getSelectionModel().selectFirst();

        creationsListView.setItems(sortedCreations);
        creationsListView.setCellFactory(new CreationCellFactory());
        creationsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Creation>() {
            @Override
            public void changed(ObservableValue<? extends Creation> observable, Creation oldValue, Creation newValue) {
                if (newValue != null && newValue != oldValue && newValue != MediaSingleton.getInstance().getCreation()) {
                    MediaSingleton.getInstance().setCreation(newValue);
                    try {
                        loadScene("/VideoView.fxml");
                    } catch (IOException e) {
                        // TODO - Handle exception
                    }
                    deleteButton.setDisable(false);
                }
                if (newValue == null) {
                    deleteButton.setDisable(true);
                }
            }
        });

        deleteButton.setDisable(true);
    }

    @Override
    protected String handle(SwitchSceneEvent event) {
        try {
            loadScene(event.getNext());
        } catch (IOException e) {
            // TODO - Handle exception
        }
        return null;
    }

    @Override
    public void handle(CreationProcessEvent event) {
        if (event.getStatus() == CreationProcessEvent.Status.BEGIN) {
            creationsListView.getSelectionModel().clearSelection();
            creationsListView.setDisable(true);
            sortDropdown.setDisable(true);
            createButton.setDisable(true);

            try {
                loadScene("/SearchView.fxml");
            } catch (IOException e) {
                // TODO - Handle exception
            }
        } else {
            sortDropdown.setDisable(false);
            createButton.setDisable(false);
            creationsListView.setDisable(false);

            try {
                loadScene("/WelcomeView.fxml");
            } catch (IOException e) {
                // TODO - Handle exception
            }

//            if (event.getStatus() == CreationProcessEvent.Status.CREATE) {
//                 TODO - Select new creation
//            }
        }
    }

    @FXML
    public void loadScene(String fxml) throws IOException {
        load = new FXMLLoader(this.getClass().getResource(fxml));
        GridPane test = load.load();
        Controller controller = load.getController();
        controller.setListener(this);
        adaptiveArea.setCenter(test);
    }

    @FXML public void pressCreate() {
        handle(new CreationProcessEvent(this, CreationProcessEvent.Status.BEGIN));
    }

    @FXML public void pressDelete() {
        Creation creation = creationsListView.getSelectionModel().selectedItemProperty().get();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("Are you sure you want to delete \"%s\"?", creation.getName()),
                ButtonType.YES, ButtonType.CANCEL);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            CreationManager.getInstance().delete(creation);
        }
    }

}
