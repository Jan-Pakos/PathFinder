package se.su.inlupp;

import java.util.*;

public class ListGraph<T> implements Graph<T> {

  private final Map<T,Set<ClassEdge>> nodes = new HashMap<>();

  @Override
  public void add(T node) {
    if (!nodes.containsKey(node))
      nodes.put(node, new HashSet<>());
  }

  @Override
  public void connect(T node1, T node2, String name, int weight) {
    if (weight < 0){
      throw new IllegalArgumentException();
    }
// om noden man skickar in som parameter inte finns i våran map
    if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
      throw new NoSuchElementException();
    }

// hämta kanterna och lägg dem i Sets för att förenkla senare steg
    Set<ClassEdge> edgesNode1 = nodes.get(node1);
    Set<ClassEdge> edgesNode2 = nodes.get(node2);

// iterera genom alla kanter i nod1 och kolla om någon pekar på nod2
    for (ClassEdge edge : edgesNode1){
      if(edge.getDestination().equals(node2)){
        throw new IllegalStateException();
      }
    }

// iterera genom alla kanter i nod2 och kolla om någon pekar på nod1
    for (ClassEdge edge : edgesNode2){
      if(edge.getDestination().equals(node1)){
        throw new IllegalStateException();
      }
    }

// om noderna inte är kopplade och finns, skapa kanterna och koppla dem
// skapa kant node1 -> node2
    edgesNode1.add(new ClassEdge(weight, node2, name));
// skapa kant node2 -> node1
    edgesNode2.add(new ClassEdge(weight, node1, name));
  }

  @Override
  public void setConnectionWeight(T node1, T node2, int weight) {
    if (weight < 0){
      throw new IllegalArgumentException();
    }

// om noden man skickar in som parameter inte finns i våran map
    if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
      throw new NoSuchElementException();
    }

// sätter samma vikt åt båda hållen
    getEdgeBetween(node1, node2).setWeight(weight);
    getEdgeBetween(node2, node1).setWeight(weight);
  }

  @Override
  public Set<T> getNodes() {
    return new HashSet<>(nodes.keySet());
  }


  @Override
  public Collection<Edge<T>> getEdgesFrom(T node) {
    if (!nodes.containsKey(node)) {
      throw new NoSuchElementException();
    }
    Set<Edge<T>> edges = new HashSet<>();
    for (ClassEdge edge : nodes.get(node)) {
      edges.add((Edge<T>) edge); // Explicit cast
    }
    return edges;
  }

  @Override
  public Edge<T> getEdgeBetween(T node1, T node2) {
// om noden man skickar in som parameter inte finns i våran map
    if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
      throw new NoSuchElementException();
    }
// hämta kanterna från nod1
    Collection<Edge<T>> edgesNode1 = getEdgesFrom(node1);
// iterera genom alla kanter för node1 och returnera kanten som har node2 som destination
    for (Edge<T> edge: edgesNode1){
      if (edge.getDestination().equals(node2)){
        return edge;
      }
    }
    return null;
  }

  @Override
  public void disconnect(T node1, T node2) {
// om noden man skickar in som parameter inte finns i våran map
    if (!nodes.containsKey(node1) || !nodes.containsKey(node2)) {
      throw new NoSuchElementException();
    }

// kolla om det finns en kant node1 -> node2 OCH node2 -> node1, OM INTE => throw exception
    if (getEdgeBetween(node1,node2) == null || getEdgeBetween(node2,node1) == null){
      throw new IllegalStateException();
    }

// hämta kanterna och lägg dem i Sets för att förenkla senare steg
    Set<ClassEdge> edgesNode1 = nodes.get(node1);
    Set<ClassEdge> edgesNode2 = nodes.get(node2);
// iterera genom node1 Set för att ta bort kanterna som pekar på nod2
    edgesNode1.remove(getEdgeBetween(node1,node2));
// iterera genom node2 Set för att ta bort kanterna som pekar på nod1
    edgesNode2.remove(getEdgeBetween(node2,node1));
  }

  @Override
  public void remove(T node) {
// om noden inte finns, kasta exception
    if (!nodes.containsKey(node)) {
      throw new NoSuchElementException("Node " + node + " does not exist in the graph.");
    }
// om ingen exception kastas kör resten av koden
    nodes.remove(node);
// iterera genom Set "nodes" och kolla varje nodes kanter om de pekar på våran node

    for (Set<ClassEdge> edgeSet : nodes.values()) {

// om kanten pekar på våran node -> ta bort den

      edgeSet.removeIf(edge -> node.equals(edge.getDestination())); // Corrected: use .equals for object comparison

    }
  }

  @Override
  public boolean pathExists(T from, T to) {
    return dfs(from, to, new HashSet<>());
  }

  @Override
  public List<Edge<T>> getPath(T from, T to) {
    // Check if the 'from' or 'to' nodes exist in the graph.
    // If not, a path cannot exist.
    if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
      return null; // Or throw NoSuchElementException, depending on desired contract.
    }

    // If the start node is the same as the end node,
    // the path consists of no edges.
    if (from.equals(to)) {
      return new LinkedList<>(); // Return an empty list of edges.
    }

    // Queue for the BFS algorithm. It stores nodes to visit.
    Queue<T> queue = new LinkedList<>();
    // Map to store the predecessor of each visited node.
    // Key: a node, Value: the node from which we reached the Key node.
    // This map is essential for reconstructing the path later.
    Map<T, T> predecessors = new HashMap<>();
    // Set to keep track of visited nodes to avoid cycles and redundant processing.
    Set<T> visited = new HashSet<>();

    // Start the BFS from the 'from' node.
    queue.add(from);
    visited.add(from);
    // The 'from' node has no predecessor in the path starting from itself.

    T currentNode = null;
    boolean pathFound = false;

    // Main BFS loop: continues as long as there are nodes to visit in the queue.
    while (!queue.isEmpty()) {
      currentNode = queue.poll(); // Dequeue the current node to explore.

      // If the current node is the destination 'to', the path has been found.
      if (currentNode.equals(to)) {
        pathFound = true;
        break; // Exit the BFS loop.
      }

      // Get all outgoing edges from the current node.
      Collection<Edge<T>> outgoingEdges = getEdgesFrom(currentNode);
      if (outgoingEdges != null) { // Check if there are edges from the current node
        for (Edge<T> edge : outgoingEdges) {
          T neighbor = edge.getDestination(); // Get the neighbor connected by this edge.

          // If the neighbor has not been visited yet:
          if (!visited.contains(neighbor)) {
            visited.add(neighbor); // Mark the neighbor as visited.
            predecessors.put(neighbor, currentNode); // Record 'currentNode' as the predecessor of 'neighbor'.
            queue.add(neighbor); // Enqueue the neighbor for future exploration.
          }
        }
      }
    }

    // If the destination 'to' was not reached after the BFS, no path exists.
    if (!pathFound) {
      return null; // Return null indicating no path found.
    }

    // Reconstruct the path by backtracking from 'to' to 'from' using the 'predecessors' map.
    LinkedList<Edge<T>> path = new LinkedList<>();
    T step = to; // Start reconstructing from the destination node.

    // Traverse backwards from 'to' until 'from' is reached.
    // 'from' will not be a key in 'predecessors' map if it's the start of the path with no parent.
    while (predecessors.containsKey(step)) {
      T previousNode = predecessors.get(step); // Get the predecessor of the current 'step'.

      // Retrieve the edge that connects 'previousNode' to 'step'.
      Edge<T> edgeInPath = getEdgeBetween(previousNode, step);

      if (edgeInPath == null) {
        // This case should ideally not happen in a consistent graph if a path was found.
        // It indicates an issue, possibly if getEdgeBetween doesn't find an existing edge.
        System.err.println("Error: Edge not found between " + previousNode + " and " + step + " during path reconstruction.");
        return null; // Or throw an IllegalStateException.
      }

      path.addFirst(edgeInPath); // Add the edge to the beginning of the path list.
      step = previousNode; // Move to the predecessor node for the next iteration.
    }

    // The loop terminates when 'step' becomes 'from' (as 'from' has no entry in predecessors map leading to it).
    // If path is empty here AND from!=to, it might mean 'to' was 'from's direct unvisited child in a specific scenario,
    // but the logic should correctly build up the path. The primary check is pathFound.

    return path; // Return the reconstructed path.
  }

  @Override
  public String toString(){
    StringBuilder stringToReturn = new StringBuilder();
    stringToReturn.append("Nodes:\n");
    for (Map.Entry<T, Set<ClassEdge>> entry : nodes.entrySet()){
      stringToReturn.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
    }
    return stringToReturn.toString();
  }

  // hjälpmetod som returnerar Hashset med alla grannar för en node
  private HashSet<T> getNeighbors(T node){
    HashSet<T> neighbors = new HashSet<>();
    if(nodes.get(node) != null) {
      for (ClassEdge edge : nodes.get(node)) {
        neighbors.add((T) edge.getDestination());
      }
      return neighbors;
    }
    return neighbors;
  }

  // Depth-first-search hjälpmetod
  private boolean dfs(T current, T target, Set<T> visited){
    if (current.equals(target)) return true;
    if (visited.contains(current)) return false;

    visited.add(current);
    for (T neighbor: getNeighbors(current)){
      if (dfs(neighbor, target, visited)) return true;
    }
    return false;
  }
}

