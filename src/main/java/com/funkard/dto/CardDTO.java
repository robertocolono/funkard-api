package com.funkard.dto;

import lombok.Data;

@Data
public class CardDTO {
    private String id;
    private String name;
    private String setName;
    private String rarity;
    private Integer grade;
    private String imageUrl;
    private Double marketValue;
}