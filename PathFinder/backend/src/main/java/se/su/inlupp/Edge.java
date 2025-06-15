// PROG2 VT2025, Inlämningsuppgift, del 1
// Grupp 035
// Jan Pakos japa4307
// Kimberlie Jonasson kijo0676
// Sebastian Edin seed7542
package se.su.inlupp;

public interface Edge<T> {

  int getWeight();

  void setWeight(int weight);

  T getDestination();

  String getName();
}
