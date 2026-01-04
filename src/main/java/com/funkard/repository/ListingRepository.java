package com.funkard.repository;

import com.funkard.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {
    
    /**
     * 🔍 Filtra listing per category della Card associata
     * Join implicito: Listing.card → Card.category
     */
    @Query("SELECT l FROM Listing l WHERE l.card.category = :category")
    List<Listing> findByCardCategory(@Param("category") String category);
    
    /**
     * 🔍 Filtra listing per type della Card associata
     * Join implicito: Listing.card → Card.type
     */
    @Query("SELECT l FROM Listing l WHERE l.card.type = :type")
    List<Listing> findByCardType(@Param("type") String type);
    
    /**
     * 🔍 Filtra listing per category e type della Card associata
     * Join implicito: Listing.card → Card.category AND Card.type
     */
    @Query("SELECT l FROM Listing l WHERE l.card.category = :category AND l.card.type = :type")
    List<Listing> findByCardCategoryAndType(@Param("category") String category, @Param("type") String type);
}
