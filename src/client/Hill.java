package flightclub.client;

public class Hill implements LiftSource {
    public float[] getP() {return new float[] {0, 0, 0};}
    public float getLift(float[] p) {return 0;}
    public float getLift(){return 0;}	
    public boolean contains(float[] p){return false;}
    public boolean isActive(){return true;}
    public boolean isActive(float t) {return true;}
    Circuit getCircuit() { return new Circuit(2); }
}
