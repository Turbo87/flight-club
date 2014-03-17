package flightclub.client;

interface LiftSource {
    float[] getP();
    public float getLift(float[] p);
    public boolean contains(float[] p);
    public boolean isActive();
    public boolean isActive(float t);
    public float getLift();
}
