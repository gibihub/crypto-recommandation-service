package org.crypto.recommendations.crypto_recommendation_service.repository;

import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CryptoPriceRepository extends JpaRepository<CryptoPrice, Long> {
    List<CryptoPrice> findBySymbol(String symbol);
    List<CryptoPrice> findByTimestampBetween(Instant start, Instant end);

    @Query("SELECT MIN(p.price), MAX(p.price), p.timestamp FROM CryptoPrice p WHERE p.symbol = :symbol")
    Object findMinMaxBySymbol(@Param("symbol") String symbol);
}
