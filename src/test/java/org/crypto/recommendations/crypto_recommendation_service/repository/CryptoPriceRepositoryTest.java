package org.crypto.recommendations.crypto_recommendation_service.repository;

import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CryptoPriceRepositoryTest {

    @Autowired
    private CryptoPriceRepository cryptoPriceRepository;

    private CryptoPrice btcPrice1;
    private CryptoPrice btcPrice2;
    private CryptoPrice ethPrice1;
    private CryptoPrice ethPrice2;

    @BeforeEach
    void setUp() {
        btcPrice1 = new CryptoPrice(null, "BTC", 30000.0, Instant.parse("2023-01-01T10:00:00Z"));
        btcPrice2 = new CryptoPrice(null, "BTC", 35000.0, Instant.parse("2023-01-01T12:00:00Z"));
        ethPrice1 = new CryptoPrice(null, "ETH", 1000.0, Instant.parse("2023-01-01T10:00:00Z"));
        ethPrice2 = new CryptoPrice(null, "ETH", 1200.0, Instant.parse("2023-01-01T12:00:00Z"));

        cryptoPriceRepository.saveAll(List.of(btcPrice1, btcPrice2, ethPrice1, ethPrice2));
    }

    @Test
    void testFindBySymbol() {
        List<CryptoPrice> btcPrices = cryptoPriceRepository.findBySymbol("BTC", Sort.by("timestamp").ascending());
        assertEquals(2, btcPrices.size());
        assertEquals(30000.0, btcPrices.get(0).getPrice());
        assertEquals(35000.0, btcPrices.get(1).getPrice());
    }

    @Test
    void testFindBySymbol_Paginated() {
        List<CryptoPrice> btcPrices = cryptoPriceRepository.findBySymbol("BTC", PageRequest.of(0, 1));
        assertEquals(1, btcPrices.size());
        assertEquals(30000.0, btcPrices.get(0).getPrice());
    }

    @Test
    void testFindByTimestampBetween() {
        Instant start = Instant.parse("2023-01-01T09:00:00Z");
        Instant end = Instant.parse("2023-01-01T13:00:00Z");

        List<CryptoPrice> prices = cryptoPriceRepository.findByTimestampBetween(start, end);
        assertEquals(4, prices.size());
    }

    @Test
    void testFindByTimestampBetween_Sorted() {
        Instant start = Instant.parse("2023-01-01T09:00:00Z");
        Instant end = Instant.parse("2023-01-01T13:00:00Z");

        List<CryptoPrice> prices = cryptoPriceRepository.findByTimestampBetween(start, end, Sort.by("price").descending());
        assertEquals(4, prices.size());
        assertEquals(35000.0, prices.get(0).getPrice());
    }

    @Test
    void testFindLatestBySymbol() {
        Optional<CryptoPrice> latestBtcPrice = cryptoPriceRepository.findLatestBySymbol("BTC");
        assertTrue(latestBtcPrice.isPresent());
        assertEquals(35000.0, latestBtcPrice.get().getPrice());
    }

    @Test
    void testFindAveragePriceBySymbolAndTimestampBetween() {
        Instant start = Instant.parse("2023-01-01T09:00:00Z");
        Instant end = Instant.parse("2023-01-01T13:00:00Z");

        Optional<Double> avgPrice = cryptoPriceRepository.findAveragePriceBySymbolAndTimestampBetween("BTC", start, end);
        assertTrue(avgPrice.isPresent());
        assertEquals(32500.0, avgPrice.get());
    }
}
