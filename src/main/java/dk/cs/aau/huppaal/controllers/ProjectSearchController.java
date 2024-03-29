package dk.cs.aau.huppaal.controllers;

import com.jfoenix.controls.JFXTextField;
import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.abstractions.Component;
import dk.cs.aau.huppaal.abstractions.Edge;
import dk.cs.aau.huppaal.abstractions.Location;
import dk.cs.aau.huppaal.presentations.ProjectSearchResultPresentation;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ProjectSearchController implements Initializable {
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
            var newLabels = new ArrayList<ProjectSearchResultPresentation>();
            Pattern searchTerm;
            try {
                searchTerm = Pattern.compile(newValue, Pattern.CASE_INSENSITIVE);
            } catch (Exception e) {
                searchTerm = Pattern.compile(newValue, Pattern.CASE_INSENSITIVE | Pattern.LITERAL);
            }

            var components = getComponentNames(searchTerm);
            for(var c : components) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new ProjectSearchResultPresentation(c).withClickEffect(this::closeStage));
            }

            var locations = getLocations(searchTerm);
            for (var l : locations) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new ProjectSearchResultPresentation(l.getKey(), l.getValue()).withClickEffect(this::closeStage));
            }

            var edges = getEdges(searchTerm);
            for (var e : edges) {
                if(newLabels.size() < maxSearchSize)
                    newLabels.add(new ProjectSearchResultPresentation(e.getKey(), e.getValue()).withClickEffect(this::closeStage));
            }

            Platform.runLater(() -> {
                resultsBox.getChildren().clear();
                resultsBox.getChildren().addAll(newLabels);
            });
        });

        searchTextField.setOnAction(e -> {
            if(resultsBox.getChildren() != null && resultsBox.getChildren().size() > 0)
                ((ProjectSearchResultPresentation) resultsBox.getChildren().get(0)).click();
        });
    }

    private void closeStage() {
        HUPPAAL.toggleSearchModal.run();
    }

    private List<Component> getComponentNames(Pattern searchVal) {
        return HUPPAAL.getProject().getComponents().stream()
                .filter(c -> searchVal.matcher(c.getName()).find())
                .toList();
    }

    private List<Pair<Component,Location>> getLocations(Pattern searchVal) {
        return HUPPAAL.getProject().getComponents().stream()
                .map(c -> c.getLocations().stream()
                        .filter(l ->
                                searchVal.matcher(l.getId()).find() ||
                                searchVal.matcher(l.getNickname()).find())
                        .map(l -> new Pair<>(c,l))
                        .toList())
                .flatMap(List::stream)
                .toList();
    }

    private List<Pair<Component, Edge>> getEdges(Pattern searchVal) {
        return HUPPAAL.getProject().getComponents().stream()
                .map(c -> c.getEdges().stream()
                        .filter(e -> searchVal.matcher(e.getUpdate()).find() ||
                                     searchVal.matcher(e.getGuard()).find() ||
                                     searchVal.matcher(e.getSelect()).find() ||
                                     searchVal.matcher(e.getSync()).find() ||
                                     matchesEdgeSourceOrTarget(e, searchVal))
                        .map(e -> new Pair<>(c,e))
                        .toList())
                .flatMap(List::stream)
                .toList();
    }

    private boolean matchesEdgeSourceOrTarget(Edge e, Pattern searchVal) {
        return (e.getSourceLocation() != null && searchVal.matcher(e.getSourceLocation().getId()).find()) ||
               (e.getTargetLocation() != null && searchVal.matcher(e.getTargetLocation().getId()).find()) ||
               (e.getSourceJork() != null && searchVal.matcher(e.getSourceJork().getId()).find()) ||
               (e.getTargetJork() != null && searchVal.matcher(e.getTargetJork().getId()).find()) ||
               (e.getSourceSubComponent() != null && searchVal.matcher(e.getSourceSubComponent().getIdentifier()).find()) ||
               (e.getTargetSubComponent() != null && searchVal.matcher(e.getTargetSubComponent().getIdentifier()).find());
    }
}
