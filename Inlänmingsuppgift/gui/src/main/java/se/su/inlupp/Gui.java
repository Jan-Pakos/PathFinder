package se.su.inlupp;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import java.io.File;
import java.io.IOException;

public class Gui extends Application {
  private Stage stage;
  private ImageView imageView;
  private Pane imagePane;
  private HashMap<String, ArrayList<Double>> coordinates;


  public void start(Stage primaryStage) {
    Graph<String> graph = new ListGraph<String>();
    this.stage = primaryStage;
    primaryStage.setTitle("Path Finder");
    imagePane = new Pane();

    MenuBar menuBar = new MenuBar();
    // skapa meny
    Menu menu = new Menu("File");

    // skapa menyval
    MenuItem newMapMenuItem = new MenuItem("New Map");
    MenuItem openMenuItem = new MenuItem("Open");
    MenuItem saveMenuItem = new MenuItem("Save");
    MenuItem saveImageMenuItem = new MenuItem("Save Image");
    MenuItem exitMenuItem = new MenuItem("Exit");

    // lägg till dem i menyn
    menu.getItems().addAll(newMapMenuItem, openMenuItem, saveMenuItem, saveImageMenuItem, exitMenuItem);

    // lägg till menyn i menybaren
    menuBar.getMenus().add(menu);

    // menyn som fälls ut från "file" knappen
    VBox root = new VBox(menuBar);

    // gör så att newmap knappen kan importera filer
    newMapMenuItem.setOnAction(new NewMapButton());
    openMenuItem.setOnAction(new OpenGraphButton());

    FlowPane buttonContainer = new FlowPane();
    buttonContainer.setOrientation(Orientation.HORIZONTAL);
    buttonContainer.setVgap(10);
    buttonContainer.setHgap(10);

    buttonContainer.setPadding(new Insets(20));

    //skapa knapparna
    Button findPathButton = new Button("Find Path");
    Button showConnectionButton = new Button("Show Connection");
    Button newPlaceButton = new Button("New Place");
    Button newConnectionButton = new Button("New Connection");
    Button changeConnectionButton = new Button("Change Connection");

    findPathButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    showConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    newPlaceButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    newConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    changeConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");


    // lägg till dem
    buttonContainer.getChildren().addAll(
            findPathButton,
            showConnectionButton,
            newPlaceButton,
            newConnectionButton,
            changeConnectionButton
    );


    // lägg båda scenerna i en borderpane för att visa dem i samma stage
    BorderPane combinedLayout = new BorderPane();
    combinedLayout.setCenter(buttonContainer);
    combinedLayout.setTop(menuBar);
    combinedLayout.setBottom(imagePane);


    // skapa kombinerad scene för både menubar och knapparna
    Scene combinedScene = new Scene(combinedLayout, 600, 400); // Example size


    stage.setScene(combinedScene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }


  private void errorAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }


  // hjälpmetoder för menyn
  private void fileHandler() {
    FileChooser fileChooser = new FileChooser();

    fileChooser.setTitle("Open Graph File");
    File selectedFile = fileChooser.showOpenDialog(stage);
  }



class NewMapButton implements EventHandler<ActionEvent> {

  @Override
  public void handle(ActionEvent actionEvent) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open GIF Image");

    File selectedFile = fileChooser.showOpenDialog(stage);
    if (selectedFile != null) {
      try {
        Image image = new Image(selectedFile.toURI().toURL().toExternalForm());

        if (image.isError()) {
          throw new IOException("Image failed to load from " + image.getUrl());
        }


        imageView = new ImageView(image);
        imageView.setPreserveRatio(true); // kolla närmare på om den behövs
        imagePane.getChildren().clear();
        imagePane.getChildren().add(imageView);
        stage.setHeight(image.getHeight());
        stage.setWidth(image.getWidth());

        // gör så att man inte kan ändra storleken på fönstret
        stage.setResizable(false);

      } catch (IOException er) {
        errorAlert("Failed to load image", er.getMessage());
      }
    }

  }
  }

class OpenGraphButton implements EventHandler<ActionEvent>{

  @Override
  public void handle(ActionEvent actionEvent) {
    openFile();
  }

  private void openFile(){
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
    File file = fileChooser.showOpenDialog(stage);



    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line = br.readLine();
      String imagePath = line.trim().replace("file:", "");

      System.out.println("Image Path: " + imagePath);

      File imageFile = new File(line.trim().replace("file:", ""));
      if (!imageFile.exists()) {
        errorAlert("Image not found", "Could not find image at path:\n" + imageFile.getAbsolutePath());
        return;
      }

      Image image = new Image(new FileInputStream(imageFile));
      if (image.isError()) {
        throw new IOException("Failed to load image.");
      }

      // Visa bilden
      if (imageView == null) {
        imageView = new ImageView(image);
      } else {
        imageView.setImage(image);
      }
      imagePane.getChildren().clear();
      imagePane.getChildren().add(imageView);
      stage.setHeight(image.getHeight());
      stage.setWidth(image.getWidth());
      stage.setResizable(false);




      // hashmap som ska innehålla stadnamnen (nyckeln) och dess motsvarande koordinater i arraylist
      coordinates = new HashMap<>();
      ListGraph graphOfNodes = new ListGraph();

      line = br.readLine();
      String[] fileRow =  line.split(";");
      for(int i = 0; i < fileRow.length;i+=3){
        String name = fileRow[i];

        // skapa lista för koordinaterna, lägga in koordinat X och Y i listan
        ArrayList<Double> coordinateList = new ArrayList<>();
        coordinateList.add(Double.parseDouble(fileRow[i + 1]));
        coordinateList.add(Double.parseDouble(fileRow[i + 2]));

        // lägga in nyckeln och dens motsvarande koordinater (i arraylist) i hashmapen
        coordinates.put(fileRow[i], coordinateList);

        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(Double.parseDouble(fileRow[i+1]), Double.parseDouble(fileRow[i+2]), 5, Color.BLUE);
        imagePane.getChildren().add(circle);

      }

      while ((line = br.readLine()) != null) {


        fileRow =  line.split(";");

        String node1 = fileRow[0];
        String node2 = fileRow[1];
        String edgeName = fileRow[2];
        int weight = Integer.parseInt(fileRow[3]);

        graphOfNodes.add(node1);
        graphOfNodes.add(node2);
        graphOfNodes.connect(node1, node2, edgeName, weight);



      }
    } catch (IOException e) {
      System.err.println("Error reading file: " + e.getMessage());
    }


  }
}

}




