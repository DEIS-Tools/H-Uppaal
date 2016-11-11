package SW9.controllers;

import SW9.presentations.FilePresentation;
import com.jfoenix.controls.JFXRippler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class FilePaneController implements Initializable {

    private static final String DIRECTORY = System.getProperty("user.dir") + File.separator + "project";

    public StackPane root;
    public AnchorPane toolbar;
    public Label toolbarTitle;
    public ScrollPane scrollPane;
    public VBox filesList;
    public JFXRippler createComponent;
    public JFXRippler saveProject;

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        createFilesInProjectFolder(); // todo: these files are created for testing purposes

        final File folder = new File(DIRECTORY);
        listFilesForFolder(folder);
    }

    private void listFilesForFolder(final File folder) {

        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                final FilePresentation filePresentation = new FilePresentation(fileEntry);
                filesList.getChildren().add(filePresentation);
            }
        }

    }

    private void createFilesInProjectFolder() {
        final String[] files = new String[]{
                "component0.json",
                "component1.json",
                "component2.json"
        };

        for (final String file : files) {
            final File myFile = new File(DIRECTORY + File.separator + file);
            final File parentDir = myFile.getParentFile();

            // create parent dir and ancestors if necessary
            if (!parentDir.exists()) parentDir.mkdirs();

            try {
                final Writer w = new OutputStreamWriter(new FileOutputStream(myFile), "UTF-8");
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void createComponentClicked() {
        System.out.println("createComponentClicked");
    }

    @FXML
    private void saveProjectClicked() {
        System.out.println("saveProjectClicked");
    }

}
