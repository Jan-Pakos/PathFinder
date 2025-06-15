# Map Path Finder

This is a JavaFX application that allows you to load map images and then create and manage a graph of interconnected locations on those maps. You can mark places, define connections (like roads or routes) between them with associated travel times, and even find the shortest path between two points.

---

## Features

* **Load Custom Maps:** Start by loading any image file (e.g., PNG, JPEG) as your map background.
* **Place Markers:** Click on the map to add new "places" (nodes/locations) at specific coordinates.
* **Connect Locations:** Select two marked places and define a new connection (edge) between them, specifying a name (e.g., "Highway A") and a travel time in hours.
* **View Connection Details:** Get information about existing connections between two selected places.
* **Change Connection Time:** Update the travel time for an existing connection.
* **Find Shortest Path:** Calculate and display the total time and route for the shortest path between two selected locations.
* **Save & Load Graphs:**
    * **Save Graph:** Store your created locations and connections to a `.graph` file for later use. This file also stores the path to your map image, so it can be reloaded.
    * **Save Image:** Save a screenshot of your current map with all drawn places and connections as a PNG image.
* **Unsaved Changes Warning:** The application will warn you if you try to exit or load a new map without saving your current graph.

---

## How to Use

### Getting Started

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
    cd YOUR_REPO_NAME
    ```
2.  **Open in an IDE:** Import the project into your preferred Java IDE (like IntelliJ IDEA, Eclipse, or VS Code). Ensure your IDE is configured with **Java 17 (or newer)** and **JavaFX SDK**.
    * If using Maven or Gradle, ensure the JavaFX dependencies are correctly added to your `pom.xml` or `build.gradle` file.
3.  **Run the `Gui` class:** Locate `se.su.inlupp.Gui.java` and run its `main` method.

### Application Workflow

1.  **Load a Map:**
    * Go to `File` -> `New Map`.
    * Select an image file (e.g., a map picture) from your computer. The application window will resize to fit the image.
    * Once a map is loaded, the interaction buttons will become active.
2.  **Add New Places:**
    * Click the **"New Place"** button.
    * Your mouse cursor will change to a crosshair. Click on the desired location on the map.
    * A dialog will appear prompting you to enter a name for the new place. Enter a name and click "OK". A blue circle will appear at the clicked location.
3.  **Create Connections:**
    * Click on two blue circles (places) on the map. They will turn **red** to indicate they are selected.
    * Click the **"New Connection"** button.
    * A dialog will appear asking for the connection's name (e.g., "Road 123") and the time it takes (in hours). Enter the details and click "Connect".
    * A grey line will appear connecting the two places, and their circles will revert to blue.
4.  **Show Connection Details:**
    * Select two connected places (they turn red).
    * Click the **"Show Connection"** button to see the connection's name and travel time.
5.  **Change Connection Time:**
    * Select two connected places.
    * Click the **"Change Connection"** button.
    * Enter the new travel time in the dialog.
6.  **Find Path:**
    * Select two places.
    * Click the **"Find Path"** button to see the shortest path (in terms of total hours) and the segments of the journey.
7.  **Save Your Graph:**
    * Go to `File` -> `Save`.
    * Choose a location and name for your `.graph` file. This file saves all your placed locations and connections.
8.  **Open a Saved Graph:**
    * Go to `File` -> `Open`.
    * Select a previously saved `.graph` file. The application will load the associated map image and all saved places and connections.
9.  **Save Image:**
    * Go to `File` -> `Save Image` to save a screenshot of the current map with all drawn elements.
10. **Exit:**
    * Go to `File` -> `Exit` or click the window's close button (`X`).
    * If you have unsaved changes, you'll be prompted to confirm before quitting.

---

## Contributing

Feel free to fork this repository, open issues, or submit pull requests if you have suggestions for improvements or find any bugs!

---
