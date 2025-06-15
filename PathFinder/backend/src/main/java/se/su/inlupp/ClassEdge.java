// PROG2 VT2025, Inlämningsuppgift, del 1
// Grupp 035
// Jan Pakos japa4307
// Kimberlie Jonasson kijo0676
// Sebastian Edin seed7542

package se.su.inlupp;

public class ClassEdge<T> implements Edge<T> {
    private int weight;
    private final T destination;
    private final String name;


    public ClassEdge(int weight, T destination, String name ){
        this.weight = weight;
        this.destination = destination;
        this.name = name;
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public void setWeight(int weight) {
        if(weight < 0){
            throw new IllegalArgumentException();
        }
        this.weight = weight;
    }

    @Override
    public T getDestination() {
        return destination;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString(){
        return "till " + destination + " med " + name + " tar " + weight;
    }
}
