// PROG2 VT2025, Inlämningsuppgift, del 1
// Grupp 035
// Jan Pakos japa4307
// Kimberlie Jonasson kijo0676
// Sebastian Edin seed7542
package se.su.inlupp;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface Graph<T> {

  void add(T node);

  void connect(T node1, T node2, String name, int weight);

  void setConnectionWeight(T node1, T node2, int weight);

  Set<T> getNodes();

  Collection<Edge<T>> getEdgesFrom(T node);

  Edge<T> getEdgeBetween(T node1, T node2);

  void disconnect(T node1, T node2);

  void remove(T node);

  boolean pathExists(T from, T to);

  List<Edge<T>> getPath(T from, T to);
}
