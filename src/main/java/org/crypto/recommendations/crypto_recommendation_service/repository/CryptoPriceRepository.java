package org.crypto.recommendations.crypto_recommendation_service.repository;

import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {

    // Find by symbol with sorting and pagination for efficiency with large data
    List<CryptoPrice> findBySymbol(String symbol, Pageable pageable);
    List<CryptoPrice> findBySymbol(String symbol, Sort sort);

    // Find by timestamp range with sorting and pagination for optimized queries
    List<CryptoPrice> findByTimestampBetween(Instant start, Instant end, Pageable pageable);
    List<CryptoPrice> findByTimestampBetween(Instant start, Instant end, Sort sort);

    // Custom query to find latest price by symbol, using Optional for null-safe handling
    @Query("SELECT cp FROM CryptoPrice cp WHERE cp.symbol = :symbol ORDER BY cp.timestamp DESC")
    Optional<CryptoPrice> findLatestBySymbol(String symbol);

    // Custom query for average price within a given time range for a specific symbol
    @Query("SELECT AVG(cp.price) FROM CryptoPrice cp WHERE cp.symbol = :symbol AND cp.timestamp BETWEEN :start AND :end")
    Optional<Double> findAveragePriceBySymbolAndTimestampBetween(String symbol, Instant start, Instant end);
}
