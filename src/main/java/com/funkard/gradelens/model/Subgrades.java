package com.funkard.gradelens.model;

public class Subgrades {
    public double centering;
    public double corners;
    public double edges;
    public double surface;

    public Subgrades() {}

    public Subgrades(double centering, double corners, double edges, double surface) {
        this.centering = centering;
        this.corners = corners;
        this.edges = edges;
        this.surface = surface;
    }
}
