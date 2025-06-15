// PROG2 VT2025, Inlämningsuppgift, del 2
// Grupp 035
// Jan Pakos japa4307
// Kimberlie Jonasson kijo0676
// Sebastian Edin seed7542

package se.su.inlupp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import static javafx.scene.paint.Color.BLUE;
import static javafx.scene.paint.Color.RED;

public class Gui extends Application {
  private Stage stage;
  private ImageView imageView;
  private Pane imagePane;
  private boolean unsavedChanges; // för att hålla koll på om det finns osparade ändringar
  private Button findPathButton;
  private Button showConnectionButton;
  private Button newPlaceButton;
  private Button changeConnectionButton;
  private Button newConnectionButton;
  private HashMap<Location, javafx.scene.shape.Circle> placeCircles; // för att koppla cirklar till locations
  private String imagePath;
  private double imageSizeX;
  private double imageSizeY;

  private List<javafx.scene.shape.Circle> markedPlaces = new ArrayList<>(2);
  ListGraph<Location> graphOfNodes = new ListGraph<>();

  public void start(Stage primaryStage) {
    this.stage = primaryStage;
    primaryStage.setTitle("Path Finder");
    imagePane = new Pane();
    placeCircles = new HashMap<>();

    // skapa meny
    MenuBar menuBar = new MenuBar();
    // skapa meny
    Menu menu = new Menu("File");

    // skapa menyval i rullgardinen
    MenuItem newMapMenuItem = new MenuItem("New Map");
    MenuItem openMenuItem = new MenuItem("Open");
    MenuItem saveMenuItem = new MenuItem("Save");
    MenuItem saveImageMenuItem = new MenuItem("Save Image");
    MenuItem exitMenuItem = new MenuItem("Exit");

    // vad som händer om klickar på röda krysset (stänger ner fönstret)
    stage.setOnCloseRequest(e -> {
      if (unsavedChanges) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText(null);
        alert.setContentText("You have unsaved changes. Do you want to quit without saving?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
          e.consume(); // avbryt stängning
        }
      }
    });

    // lägg till dem i menyn
    menu.getItems().addAll(newMapMenuItem, openMenuItem, saveMenuItem, saveImageMenuItem, exitMenuItem);

    // lägg till menyn i menybaren
    menuBar.getMenus().add(menu);

    // definierar vad menyvalen gör
    newMapMenuItem.setOnAction(new NewMapMenuOption());
    openMenuItem.setOnAction(new OpenGraphMenuOption());
    saveMenuItem.setOnAction(new SaveGraphMenuOption());
    saveImageMenuItem.setOnAction(new SaveImageMenuOption());
    exitMenuItem.setOnAction(new ExitMenuOption());


    // skapa flowpane container för att lägga knapparna i
    FlowPane buttonContainer = new FlowPane();
    buttonContainer.setOrientation(Orientation.HORIZONTAL);
    buttonContainer.setVgap(10);
    buttonContainer.setHgap(10);

    buttonContainer.setPadding(new Insets(20));

    // instansiera knapparna
    findPathButton = new Button("Find Path");
    showConnectionButton = new Button("Show Connection");
    newPlaceButton = new Button("New Place");
    newConnectionButton = new Button("New Connection");
    changeConnectionButton = new Button("Change Connection");

    findPathButton.setDisable(true);
    showConnectionButton.setDisable(true);
    newPlaceButton.setDisable(true);
    newConnectionButton.setDisable(true);
    changeConnectionButton.setDisable(true);

    // om man klickar på knapparna, så skapas ett objekt som hanterar vad som skall hända
    newPlaceButton.setOnAction(new NewPlaceButton());
    newConnectionButton.setOnAction(new NewConnectionButton());
    showConnectionButton.setOnAction(new ShowConnectionButton());
    changeConnectionButton.setOnAction(new ChangeConnectionButton());
    findPathButton.setOnAction(new FindPathButton());

    // styling av knapparna
    findPathButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    showConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    newPlaceButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    newConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");
    changeConnectionButton.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #000000;");


    // lägg till dem i flowpane
    buttonContainer.getChildren().addAll(
            findPathButton,
            showConnectionButton,
            newPlaceButton,
            newConnectionButton,
            changeConnectionButton
    );


    // lägg alla tre scenerna i en borderpane för att visa dem i samma stage
    BorderPane combinedLayout = new BorderPane();
    combinedLayout.setCenter(buttonContainer);
    combinedLayout.setTop(menuBar);
    combinedLayout.setBottom(imagePane);

    // skapa kombinerad scene för både menubar och knapparna
    Scene combinedScene = new Scene(combinedLayout, imageSizeX, imageSizeY);

    stage.setScene(combinedScene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }


  // ##########################################################################################
//                             EVENT HANDLERS (NESTLADE KLASSER)
// ##########################################################################################
  class NewMapMenuOption implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Open Image");

      File selectedFile = fileChooser.showOpenDialog(stage);
      if (selectedFile != null) {
        try {
          // hämtar bilden från samma mapp som graph filen är i
          Image image = new Image(selectedFile.toURI().toURL().toExternalForm());
          saveImagePath(image);

          if (image.isError()) {
            throw new IOException("Image failed to load from " + image.getUrl());
          }

          findPathButton.setDisable(false);
          showConnectionButton.setDisable(false);
          newPlaceButton.setDisable(false);
          newConnectionButton.setDisable(false);
          changeConnectionButton.setDisable(false);

          // sätter fönstret till samma storlek som bilden
          imageView = new ImageView(image);
          imageView.setPreserveRatio(true);
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

  class OpenGraphMenuOption implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent actionEvent) {
      openFile();
    }

    private void openFile() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
      File graphFile = fileChooser.showOpenDialog(stage);

      if (graphFile == null) {
        return;
      }

      try (BufferedReader br = new BufferedReader(new FileReader(graphFile))) {
        String line = br.readLine().trim();


        File imageFile = new File(graphFile.getParentFile(), line);


        File imagePathFile = new File("src/main/java/se/su/inlupp/" + imageFile.getName().replace("file:", "").replaceAll("%20", " ") );
        Image image = new Image(imagePathFile.toURI().toString());

        imagePath = imageFile.getName();



        if (image.isError()) {
          throw new IOException("Failed to load image.");
        }

        // Visa bilden
        if (imageView == null) {
          imageView = new ImageView(image);
        } else {
          imageView.setImage(image);
        }
        findPathButton.setDisable(false);
        showConnectionButton.setDisable(false);
        newPlaceButton.setDisable(false);
        newConnectionButton.setDisable(false);
        changeConnectionButton.setDisable(false);

        imageSizeX = image.getWidth();
        imageSizeY = image.getHeight();
        imagePane.getChildren().clear();
        imagePane.getChildren().add(imageView);
        stage.setHeight(image.getHeight()+100);
        stage.setWidth(image.getWidth());
        stage.setResizable(false);


        String name;
        line = br.readLine();
        String[] fileRow = line.split(";");

        // reinitialize med en tom graf
        graphOfNodes = new ListGraph<>();
        for (int i = 0; i < fileRow.length; i += 3) {
          name = fileRow[i];

          // skapa locations från inlästa filen och lägg in dem i grafen
          Location newLocation = new Location(name, Double.parseDouble(fileRow[i + 1]), Double.parseDouble(fileRow[i + 2]));
          graphOfNodes.add(newLocation);

          // skapa cirklarna
          javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(Double.parseDouble(fileRow[i + 1]), Double.parseDouble(fileRow[i + 2]), 12, BLUE);
          // skickar koordinaterna till metoden som ritar cirklarna
          drawCircle(circle);
          circle.setOnMouseClicked(event -> handlePlaceClick(circle));

          // stoppar in cirklar med motsvarande namn på staden för att kunna hämta dem senare
          placeCircles.put(newLocation, circle);

        }

        // loop som läser in och sparar kanter
        while ((line = br.readLine()) != null) {
          fileRow = line.split(";");

          String node1 = fileRow[0];
          String node2 = fileRow[1];
          String edgeName = fileRow[2];
          int weight = Integer.parseInt(fileRow[3]);

          // hämtar location objekten med hjälp metod
          Location node1Location = findLocationByName(node1);
          Location node2Location = findLocationByName(node2);

          graphOfNodes.connect(node1Location, node2Location, edgeName, weight);

          javafx.scene.shape.Line lineConnection = new javafx.scene.shape.Line(node1Location.getX(), node1Location.getY(), node2Location.getX(), node2Location.getY());
          lineConnection.setStrokeWidth(5);
          lineConnection.setStroke(Color.GREY);
          drawLine(lineConnection);
        }
      } catch (IOException e) {
        System.err.println("Error reading file: " + e.getMessage());
      }


    }
  }


  class SaveGraphMenuOption implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if ((imageView == null)) {
        errorAlert("Error", "There is nothing to save, load a graph first.");
      }
      saveGraph();
    }
    // metod för att spara grafen
    private void saveGraph() {
      FileChooser fileChooser = new FileChooser();
      File file = fileChooser.showSaveDialog(stage);

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        // hämta bildens path och skriv den på första raden
        String[] correctFilePath = imagePath.split("/");
        writer.write(imagePath.split("/")[correctFilePath.length - 1]);
        writer.newLine();

        // Lägg nycklarna i ett Set för att sedan loopa genom och skriva såhär: nyckel;koordinatX;KoordinatY.... osv
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (Location key : graphOfNodes.getNodes()) {
          stringArrayList.add(key.getName() + ";" + (key.getX()) + ";" + key.getY());
        }
        writer.write(String.join(";", stringArrayList));
        writer.newLine();

        // skapa sett för att hålla koll på vilken kant vi redan har skrivit
        Set<String> written = new HashSet<>();
        for (Location key : graphOfNodes.getNodes()) {
          for (Edge<Location> edge : graphOfNodes.getEdgesFrom(key)) {
            // kollar tex "Stockholm;Malmö" och "Malmö:Stockholm" så att bara en av dem skrivs ut i graph filen
            String edge1 = key.getName() + edge.getDestination().getName();
            String edge2 = edge.getDestination().getName() + key.getName();


            // koll som gör att man inte skriver ut dubbletter
            if (!written.contains(edge1) && !written.contains(edge2)) {
              writer.write(key.getName() + ";" + edge.getDestination().getName() + ";" + edge.getName() + ";" + edge.getWeight());
              writer.newLine();
              written.add(edge1);
            }
          }
        }

        unsavedChanges = false;

      } catch (IOException e) {
        errorAlert("Error", "Could not save graph.");
      }
    }


  }

  class SaveImageMenuOption implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if (imagePane.getChildren().isEmpty()) {
        errorAlert("Error", "There is no image to save, load a graph or image first.");
      }

      WritableImage writableImage = imagePane.snapshot(null, null);
      File currentDir = new File(System.getProperty("user.dir"));
      File parentDir = currentDir.getParentFile();
      File outPutFile = new File(parentDir, "./capture.png");
      try {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
        ImageIO.write(bufferedImage, "png", outPutFile);
      } catch (IOException e) {
        errorAlert("Error", "Could not save image.");
      }

    }


  }

  class ExitMenuOption implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if (unsavedChanges) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText(null);
        alert.setContentText("You have unsaved changes. Do you want to quit before saving?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
          return; // avbryt stängningen
        }
      }
      stage.close(); // avsluta programmet
    }
  }

  class NewPlaceButton implements EventHandler<ActionEvent> {
    private boolean isWaitingForPlaceClick = false;

    @Override
    public void handle(ActionEvent actionEvent) {

      if (imageView == null) {
        errorAlert("Error", "There is no image to draw on, load a graph or image first.");
      }
      newPlaceButton.setDisable(true);
      isWaitingForPlaceClick = true;
      imagePane.setCursor(Cursor.CROSSHAIR);

      imagePane.setOnMouseClicked(e -> {
        if (!isWaitingForPlaceClick) return;

        double x = e.getX();
        double y = e.getY();

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New Place");
        dialog.setHeaderText("Name of place:");
        dialog.setContentText("Name");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
          String name = result.get().trim();

          if (name.isEmpty()) {
            errorAlert("Error", "No name was entered");
          } else {

            Location newLocation = new Location(name, x, y);
            javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(x, y, 12, BLUE);
            circle.setOnMouseClicked(event -> handlePlaceClick(circle));
            drawCircle(circle);

            // mapar nya location till en cirkel och lägger in den i grafen
            placeCircles.put(newLocation, circle);
            graphOfNodes.add(newLocation);
            unsavedChanges = true;

          }
        }
        newPlaceButton.setDisable(false);
        isWaitingForPlaceClick = false;
        imagePane.setCursor(Cursor.DEFAULT);
      });
    }
  }

  class NewConnectionButton implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if (imageView == null) {
        errorAlert("Error", "There is no image to draw on, load a graph or image first.");
        return;
      }
      if (markedPlaces.size() < 2) {
        errorAlert("Error", "You must have at least 2 marked places to connect.");
        return;
      }

      Location locationOne = findLocationByCircle(markedPlaces.get(0));
      Location locationTwo = findLocationByCircle(markedPlaces.get(1));

      if (graphOfNodes.getEdgeBetween(locationOne, locationTwo) != null) {
        errorAlert("Connection exists", "This connection already exists.");
        return;
      }

      // skapa ett Pair objekt som kan hålla både name(som nyckel) och weight(som värde) ***
      Dialog<Pair<String, Integer>> dialog = new Dialog<>();
      dialog.setTitle("New Connection");
      dialog.setHeaderText("Connection from: " + locationOne.getName() + " to: " + locationTwo.getName());

      // skapa knapparna
      ButtonType connectButtonType = new ButtonType("Connect", ButtonBar.ButtonData.OK_DONE);
      dialog.getDialogPane().getButtonTypes().addAll(connectButtonType, ButtonType.CANCEL);

      // skapa input fields
      GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      TextField nameField = new TextField();
      nameField.setPromptText("Connection name");
      TextField weightField = new TextField();
      weightField.setPromptText("Time in hours");

      grid.add(new Label("Name:"), 0, 0);
      grid.add(nameField, 1, 0);
      grid.add(new Label("Time:"), 0, 1);
      grid.add(weightField, 1, 1);

      dialog.getDialogPane().setContent(grid);

      // fokusera på namnfältet ***
      Platform.runLater(() -> nameField.requestFocus());

      // konvertera resultatet när man klickar "OK"
      dialog.setResultConverter(dialogButton -> {
        if (dialogButton == connectButtonType) {
          String name = nameField.getText();
          String weightText = weightField.getText();

          // kolla om inmatningsfälten är tomma
          if (name == null || name.trim().isEmpty() || weightText == null || weightText.trim().isEmpty()) {
            return null;
          }

          try {
            int weight = Integer.parseInt(weightText.trim());
            if (weight < 0) throw new NumberFormatException();
            return new Pair<>(name.trim(), weight);
          } catch (NumberFormatException e) {
            return null;
          }
        }
        return null;
      });

      Optional<Pair<String, Integer>> result = dialog.showAndWait();

      if (result.isPresent()) {
        Pair<String, Integer> data = result.get();
        if (data != null) {
          String name = data.getKey();
          Integer weight = data.getValue();

          Location locationToConnectOne = findLocationByCircle(markedPlaces.get(0));
          Location locationToConnectTwo = findLocationByCircle(markedPlaces.get(1));

          // connectar två nodes och skapar linjen för kanten
          graphOfNodes.connect(locationToConnectOne, locationToConnectTwo, name, weight);
          Line lineToDraw = new Line(locationToConnectOne.getX(), locationToConnectOne.getY(), locationToConnectTwo.getX(), locationToConnectTwo.getY());
          lineToDraw.setStrokeWidth(5);
          lineToDraw.setStroke(Color.GREY);
          drawLine(lineToDraw);

          // byta cirklarnas färg och rensa markedplaces arraylist samt sätta unsavedchanges till true
          changeCircleColor(markedPlaces.get(0));
          changeCircleColor(markedPlaces.get(1));
          markedPlaces.clear();
          unsavedChanges = true;
        } else {
          errorAlert("Error", "Invalid input. Name field cannot be empty and time must be a positive number.");
        }
     } else {
       errorAlert("Empty fields", "Please enter a valid name and time.");
     }
    }
  }

  class ShowConnectionButton implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if (imageView == null) {
        errorAlert("Error", "There is no image to draw on, load a graph or image first.");
        return;
      }
      if (markedPlaces.size() < 2) {
        errorAlert("Error", "You must have at least 2 marked places to show connection.");
        return;
      }
      Location locationOne = findLocationByCircle(markedPlaces.get(0));
      Location locationTwo = findLocationByCircle(markedPlaces.get(1));
      if (graphOfNodes.getEdgeBetween(locationOne, locationTwo) == null) {
        errorAlert("Connection does not exist", "This connection does not exist.");
      }

      Edge<Location> edge = graphOfNodes.getEdgeBetween(locationOne, locationTwo);

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Connection");
      alert.setHeaderText("Connection from: " + locationOne.getName() + " to: " + locationTwo.getName());
      alert.setContentText(edge.getName() + " (" + edge.getWeight() + " hours)");
      alert.showAndWait();

    }
  }

  class ChangeConnectionButton implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent actionEvent) {
      if (imageView == null) {
        errorAlert("Error", "There is no image to draw on, load a graph or image first.");
        return;
      }
      if (markedPlaces.size() < 2) {
        errorAlert("Error", "You must have at least 2 marked places to change connection.");
        return;
      }

      Location locationOne = findLocationByCircle(markedPlaces.get(0));
      Location locationTwo = findLocationByCircle(markedPlaces.get(1));
      Edge<Location> edge = graphOfNodes.getEdgeBetween(locationOne, locationTwo);

      if (edge == null) {
        errorAlert("Error", "There is no connection between the selected places.");
        return;
      }

      TextInputDialog dialog = new TextInputDialog("");
      dialog.setTitle("Change Connection");
      dialog.setHeaderText("Connection by: " + edge.getName() +
              " from: " + locationOne.getName() +
              " to: " + locationTwo.getName() +
              " current time: " + edge.getWeight() + " hours");
      dialog.setContentText("New Time:");

      Optional<String> result = dialog.showAndWait();
      if (result.isPresent()) {
        try {
          int newWeight = Integer.parseInt(result.get().trim());
          if (newWeight < 0) throw new NumberFormatException();
          graphOfNodes.setConnectionWeight(locationOne, locationTwo, newWeight);
        } catch (NumberFormatException e) {
          errorAlert("Error", "Time must be a positive number.");
        }
      }
    }
  }

  class FindPathButton implements EventHandler<ActionEvent> {

    @Override
    public void handle(ActionEvent actionEvent) {
      if (imageView == null) {
        errorAlert("Error", "There is no graph loaded, load a graph first.");
        return;
      }
      if (markedPlaces.size() < 2) {
        errorAlert("Error", "You must have at least 2 marked places to find a path.");
        return;
      }
      Location locationOne = findLocationByCircle(markedPlaces.get(0));
      Location locationTwo = findLocationByCircle(markedPlaces.get(1));
      if (!graphOfNodes.pathExists(locationOne, locationTwo)) {
        errorAlert("Path does not exist", "There is not path between the selected places.");
      } else {
        List<Edge<Location>> edgesFromPath = graphOfNodes.getPath(locationOne, locationTwo);
        StringBuilder edgesFromPathString = new StringBuilder();
        int totalTime = 0;
        for (Edge<Location> edge : edgesFromPath) {
          edgesFromPathString.append("To: " + edge.getDestination().getName() + "   By: " + edge.getName() + "   Takes: " + edge.getWeight() + "hours");
          edgesFromPathString.append("\n");
          totalTime += edge.getWeight();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Path");
        alert.setHeaderText("Path from: " + locationOne.getName() + " to: " + locationTwo.getName());
        alert.setContentText("Path:" + "\n" + edgesFromPathString + "\nTotal time: " + totalTime + " hours" );
        alert.showAndWait();
      }
    }
  }

  // skapas objekt av för att kunna lagra noderna och dens koordinater
  class Location {
    private double x;
    private double y;
    private String name;

    public Location(String name, double x, double y) {
      this.x = x;
      this.y = y;
      this.name = name;
    }
    public double getX() {
      return x;
    }

    public double getY() {
      return y;
    }

    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;
      Location other = (Location) obj;
      return Double.compare(x, other.x) == 0 &&
              Double.compare(y, other.y) == 0 &&
              Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, y, name);
    }

  }

// ##########################################################################################
//                                        HJÄLPMETODER
// ##########################################################################################
  private void drawCircle(Circle circle) {
    imagePane.getChildren().add(circle);
  }

  private void drawLine(Line line){
    imagePane.getChildren().add(line);
  }


  private void handlePlaceClick(Circle circle) {
    if (markedPlaces.contains(circle)) {
      // Avmarkera
      changeCircleColor(circle);
      markedPlaces.remove(circle);
    } else {
      // om location inte finns i listan av markerade platser och <2 platser har markerats
      if (markedPlaces.size() < 2) {
        changeCircleColor(circle);
        markedPlaces.add(circle);
      }
      // annars görs ingenting
    }
  }

  public Location findLocationByName(String name) {
    for (Location loc : graphOfNodes.getNodes()) {
      if (loc.getName().equals(name)) {
        return loc;
      }
    }
    return null;
  }

  public Location findLocationByCircle(Circle circle) {
    for (Location loc : placeCircles.keySet()) {
      if (placeCircles.get(loc) == circle) {
        return loc;
      }
    }
    return null;
  }

  public void changeCircleColor(Circle circle) {
    if(BLUE == circle.getFill()){
      circle.setFill(RED);
    } else {
      circle.setFill(BLUE);
    }
  }

  public void saveImagePath(Image image){
    imagePath = image.getUrl();
  }

  // hjälpmetod för alla felmeddelanden
  private void errorAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }


}









