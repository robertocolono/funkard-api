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
    
    /**
     * 🔍 Filtra listing per condition
     */
    @Query("SELECT l FROM Listing l WHERE l.condition = :condition")
    List<Listing> findByCondition(@Param("condition") String condition);
    
    /**
     * 🔍 Filtra listing per category della Card associata e condition
     * Join implicito: Listing.card → Card.category
     */
    @Query("SELECT l FROM Listing l WHERE l.card.category = :category AND l.condition = :condition")
    List<Listing> findByCardCategoryAndCondition(@Param("category") String category, @Param("condition") String condition);
    
    /**
     * 🔍 Filtra listing per type della Card associata e condition
     * Join implicito: Listing.card → Card.type
     */
    @Query("SELECT l FROM Listing l WHERE l.card.type = :type AND l.condition = :condition")
    List<Listing> findByCardTypeAndCondition(@Param("type") String type, @Param("condition") String condition);
    
    /**
     * 🔍 Filtra listing per category, type della Card associata e condition
     * Join implicito: Listing.card → Card.category AND Card.type
     */
    @Query("SELECT l FROM Listing l WHERE l.card.category = :category AND l.card.type = :type AND l.condition = :condition")
    List<Listing> findByCardCategoryAndTypeAndCondition(@Param("category") String category, @Param("type") String type, @Param("condition") String condition);
    
    /**
     * 🔍 Query unificata per filtri Marketplace (category, type, condition, language, franchise, search, acceptTrades)
     * Parametri NULL = filtro non applicato
     * Join implicito: Listing.card → Card per category, type, language, franchise
     * type è multiselect: usa IN per liste, gestisce null e liste vuote
     */
    @Query("""
        SELECT l FROM Listing l
        WHERE (:category IS NULL OR l.card.category = :category)
        AND (:type IS NULL OR SIZE(:type) = 0 OR l.card.type IN :type)
        AND (:condition IS NULL OR l.condition = :condition)
        AND (:language IS NULL OR l.card.language = :language)
        AND (:franchise IS NULL OR l.card.franchise = :franchise)
        AND (:search IS NULL OR (
            LOWER(COALESCE(l.title, '')) LIKE :search
            OR LOWER(COALESCE(l.description, '')) LIKE :search
            OR LOWER(COALESCE(l.card.name, '')) LIKE :search
            OR LOWER(COALESCE(l.card.setName, '')) LIKE :search
            OR LOWER(COALESCE(l.card.franchise, '')) LIKE :search
        ))
        AND (:acceptTrades IS NULL OR l.acceptTrades = :acceptTrades)
        """)
    List<Listing> findByFilters(
        @Param("category") String category,
        @Param("type") List<String> type,
        @Param("condition") String condition,
        @Param("language") String language,
        @Param("franchise") String franchise,
        @Param("search") String search,
        @Param("acceptTrades") Boolean acceptTrades
    );
}
