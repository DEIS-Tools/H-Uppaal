package dk.cs.aau.huppaal.controllers;

import com.jfoenix.controls.JFXTextField;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.logging.Log;
import dk.cs.aau.huppaal.presentations.SpotlightSearchResultPresentation;
import dk.cs.aau.huppaal.utility.helpers.SelectHelper;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class SpotlightSearchController implements Initializable {
    public HBox root;
    public JFXTextField searchTextField;
    public VBox resultsBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFocus();
        initializeSearchTextField();
    }

    private void initializeFocus() {
        root.focusedProperty().addListener((e,o,n) -> {
            if(n) {
                searchTextField.requestFocus();
                searchTextField.selectAll();
            }
        });
    }

    private void initializeSearchTextField() {
        searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            int maxSearchSize = 100;
            var newLabels = new ArrayList<SpotlightSearchResultPresentation>();
            var searchTerm = newValue.toLowerCase();

            var components = getComponentNames(searchTerm);
            for(var c : components) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new SpotlightSearchResultPresentation(c));
            }

            var locations = getLocationsBasedOnNicknames(searchTerm);
            for (var l : locations) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new SpotlightSearchResultPresentation(l.getKey(), l.getValue()));
            }

            var edges = getEdgesBasedOnTags(searchTerm);
            for (var e : edges) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new SpotlightSearchResultPresentation(e.getKey(), e.getValue()));
            }

            Platform.runLater(() -> {
                resultsBox.getChildren().clear();
                resultsBox.getChildren().addAll(newLabels);
            });
        });
    }

    private List<Component> getComponentNames(String searchVal) {
        if(searchVal.isEmpty())
            return Collections.emptyList();
        return HUPPAAL.getProject().getComponents().stream().filter(c -> c.getName().toLowerCase().contains(searchVal)).toList();
    }

    private List<Pair<Component,Location>> getLocationsBasedOnNicknames(String searchVal) {
        return HUPPAAL.getProject().getComponents().stream()
                .map(c -> c.getLocations().stream()
                        .filter(l -> l.getNickname().toLowerCase().contains(searchVal))
                        .map(l -> new Pair<>(c,l))
                        .toList())
                .flatMap(List::stream)
                .toList();
    }

    private List<Pair<Component, Edge>> getEdgesBasedOnTags(String searchVal) {
        return HUPPAAL.getProject().getComponents().stream()
                .map(c -> c.getEdges().stream()
                        .filter(e -> (e.getUpdate().toLowerCase().contains(searchVal) || e.getGuard().toLowerCase().contains(searchVal)))
                        .map(e -> new Pair<>(c,e))
                        .toList())
                .flatMap(List::stream)
                .toList();
    }
}
