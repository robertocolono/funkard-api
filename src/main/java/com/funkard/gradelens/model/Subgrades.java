package com.funkard.gradelens.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subgrades {
    private double centering;
    private double corners;
    private double edges;
    private double surface;
}
