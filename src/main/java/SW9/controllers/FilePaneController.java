package SW9.controllers;

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

    private static String DIRECTORY = System.getProperty("user.dir") + File.separator + "project";

    public StackPane root;
    public AnchorPane toolbar;
    public Label toolbarTitle;
    public ScrollPane scrollPane;
    public VBox filesList;

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

                String name = fileEntry.getAbsolutePath();
                name = name.replace(DIRECTORY, "");

                filesList.getChildren().add(new Label(name));
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

}
