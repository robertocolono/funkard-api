package com.funkard.dto;

import com.funkard.model.FranchiseCatalog;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 📚 DTO per franchise
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FranchiseDTO {
    private Long id;
    private String category;
    private String name;
    private Boolean active;
    
    /**
     * Costruttore da entità FranchiseCatalog
     */
    public FranchiseDTO(FranchiseCatalog franchise) {
        this.id = franchise.getId();
        this.category = franchise.getCategory();
        this.name = franchise.getName();
        this.active = franchise.getActive();
    }
    
    /**
     * Costruttore da entità Franchise
     */
    public FranchiseDTO(com.funkard.model.Franchise franchise) {
        this.id = franchise.getId();
        this.category = franchise.getCategory();
        this.name = franchise.getName();
        this.active = franchise.getStatus() == com.funkard.model.Franchise.FranchiseStatus.ACTIVE;
    }
}

