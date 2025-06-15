// PROG2 VT2025, Inlämningsuppgift, del 1
// Grupp 035
// Jan Pakos japa4307
// Kimberlie Jonasson kijo0676
// Sebastian Edin seed7542

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
    // Kontrollera att båda noderna finns i grafen
    if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
      return null;
    }
    // om man vill gå från en nod till sig själv → tom väg
    if (from.equals(to)) {
      return new LinkedList<>();
    }
    // initierar BFS
    Queue<T> queue = new LinkedList<>();    // noder som ska undersökas
    Map<T, T> predecessors = new HashMap<>();    // håller koll på hur vi tog oss till varje nod
    Set<T> visited = new HashSet<>();        // noder vi redan kollat på

    queue.add(from);          // börja från startnoden
    visited.add(from);

    T currentNode = null;
    boolean pathFound = false;

    // BFS loop kör tills vi hittar "to" eller tar det slut på noder
    while (!queue.isEmpty()) {
      currentNode = queue.poll();    // ta första noden i kön

      if (currentNode.equals(to)) {  // om current node är samma som to så har vi hittat vägen
        pathFound = true;
        break;
      }

      // kolla alla grannar till nuvarande nod
      Collection<Edge<T>> edges = getEdgesFrom(currentNode);
      if (edges != null) {
        for (Edge<T> edge : edges) {
          T neighbor = edge.getDestination();    // granne via denna kant

          if (!visited.contains(neighbor)) {      // om vi inte redan kollat på grannen
            visited.add(neighbor);
            predecessors.put(neighbor, currentNode);   // kom till grannen via currentNode
            queue.add(neighbor);                         // lägg till grannen i kön
          }
        }
      }
    }

    // om vi aldrig kom fram => det finns ingen väg
    if (!pathFound) {
      return null;
    }

    // gör en väg baklänges från "to" till "from" med hjälp av predecessors
    LinkedList<Edge<T>> path = new LinkedList<>();
    T step = to;

    // gå bakåt från målnoden tills vi når startnoden
    while (predecessors.containsKey(step)) {
      T prev = predecessors.get(step);    // ta reda på var vi kom från

      // hämta kanten mellan två steg i vägen
      Edge<T> edge = getEdgeBetween(prev, step);
      if (edge == null) return null;               // om edge är null avbryt

      path.addFirst(edge);      // lägg till kanten i början av listan
      step = prev;            // backa ett steg
    }
    return path; // returna vägen
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

