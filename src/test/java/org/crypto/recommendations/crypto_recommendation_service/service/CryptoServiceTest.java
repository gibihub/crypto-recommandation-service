package org.crypto.recommendations.crypto_recommendation_service.service;

import org.crypto.recommendations.crypto_recommendation_service.config.CryptoConfig;
import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.crypto.recommendations.crypto_recommendation_service.repository.CryptoPriceRepository;
import org.crypto.recommendations.crypto_recommendation_service.util.CSVLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CryptoServiceTest {

    @Mock
    private CryptoConfig cryptoConfig;

    @Mock
    private CryptoPriceRepository cryptoPriceRepository;

    @Mock
    private CSVLoader csvLoader;

    @InjectMocks
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadCryptoData_validSymbol() throws Exception {
        String symbol = "BTC";
        String fileName = "BTC_values.csv";

        when(cryptoConfig.getSymbols()).thenReturn(Map.of(symbol, fileName));
        List<CryptoPrice> prices = List.of(new CryptoPrice(null, "BTC", 20000.0, Instant.now()));
        when(csvLoader.loadPricesFromCSV(fileName)).thenReturn(prices);

        cryptoService.loadCryptoData(symbol);

        verify(cryptoPriceRepository, times(1)).saveAll(prices);
    }

    @Test
    void testLoadCryptoData_invalidSymbol() {
        String invalidSymbol = "INVALID";
        when(cryptoConfig.getSymbols()).thenReturn(Map.of("BTC", "BTC_values.csv"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cryptoService.loadCryptoData(invalidSymbol));

        assertTrue(exception.getMessage().contains("Invalid cryptocurrency symbol"));
        verifyNoInteractions(csvLoader, cryptoPriceRepository);
    }

    @Test
    void testLoadCryptoData_illegalArgumentException() {
        String invalidSymbol = "INVALID";

        // Mock the CryptoConfig to return an empty map or a map that doesn't contain the invalidSymbol
        when(cryptoConfig.getSymbols()).thenReturn(Map.of("BTC", "BTC_values.csv", "ETH", "eth.csv"));

        // Expect IllegalArgumentException when the invalid symbol is passed
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cryptoService.loadCryptoData(invalidSymbol));

        // Verify the exception message
        assertEquals("Invalid cryptocurrency symbol: " + invalidSymbol, exception.getMessage());

        // Verify that no interactions with the repository occurred
        verify(cryptoPriceRepository, never()).saveAll(any());
    }


    @Test
    void testGetCryptoStatistics_validSymbol() {
        String symbol = "BTC";
        List<CryptoPrice> prices = List.of(
                new CryptoPrice(null, symbol,  45000.0, Instant.now().minusSeconds(3600)),
                new CryptoPrice(null, symbol, 50000.0, Instant.now())
        );

        when(cryptoPriceRepository.findBySymbol(symbol, Sort.by(Sort.Direction.ASC, "timestamp"))).thenReturn(prices);

        Map<String, Double> stats = cryptoService.getCryptoStatistics(symbol);

        assertEquals(45000.0, stats.get("min"));
        assertEquals(50000.0, stats.get("max"));
        assertNotEquals(0, stats.get("oldest"));
        assertNotEquals(0, stats.get("newest"));
    }

    @Test
    void testGetCryptoStatistics_noPrices() {
        String symbol = "BTC";
        when(cryptoPriceRepository.findBySymbol(symbol, Sort.by(Sort.Direction.ASC, "timestamp"))).thenReturn(Collections.emptyList());

        Map<String, Double> stats = cryptoService.getCryptoStatistics(symbol);

        assertEquals(0.0, stats.get("min"));
        assertEquals(0.0, stats.get("max"));
        assertEquals(0.0, stats.get("oldest"));
        assertEquals(0.0, stats.get("newest"));
    }

    @Test
    void testGetCryptoWithHighestRangeForDay_validData() {
        String date = "2023-01-01";
        Instant start = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plusSeconds(86400);

        List<CryptoPrice> prices = Arrays.asList(
                new CryptoPrice(1L, "BTC", 30000.0, start.plusSeconds(3600)),
                new CryptoPrice(2L, "BTC", 35000.0, start.plusSeconds(7200)),
                new CryptoPrice(3L, "ETH", 1000.0, start.plusSeconds(3600)),
                new CryptoPrice(4L, "ETH", 1200.0, start.plusSeconds(7200)),
                new CryptoPrice(5L, "DOGE", 0.1, start.plusSeconds(3600)),
                new CryptoPrice(6L, "DOGE", 0.11, start.plusSeconds(7200))
        );

        when(cryptoPriceRepository.findByTimestampBetween(start, end)).thenReturn(prices);

        String result = cryptoService.getCryptoWithHighestRangeForDay(date);
        assertEquals("ETH", result); // BTC has the highest normalized range (35000 - 30000) / 30000

        verify(cryptoPriceRepository, times(1)).findByTimestampBetween(start, end);
    }

    @Test
    void testGetCryptoWithHighestRangeForDay_noDataForDate() {
        String date = "2023-01-01";
        Instant start = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plusSeconds(86400);

        when(cryptoPriceRepository.findByTimestampBetween(start, end)).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cryptoService.getCryptoWithHighestRangeForDay(date));

        assertEquals("No data available for the given date: 2023-01-01", exception.getMessage());
        verify(cryptoPriceRepository, times(1)).findByTimestampBetween(start, end);
    }

    @Test
    void testGetCryptoWithHighestRangeForDay_invalidDateFormat() {
        String invalidDate = "01-01-2023";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                cryptoService.getCryptoWithHighestRangeForDay(invalidDate));

        assertEquals("Invalid date format. Expected format: yyyy-MM-dd.", exception.getMessage());
        verify(cryptoPriceRepository, never()).findByTimestampBetween(any(), any());
    }

    @Test
    void testGetCryptoWithHighestRangeForDay_minValueZeroFiltered() {
        String date = "2023-01-01";
        Instant start = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plusSeconds(86400);

        List<CryptoPrice> prices = Arrays.asList(
                new CryptoPrice(1L, "BTC", 0.0, start.plusSeconds(3600)),  // Min price is 0, should be ignored
                new CryptoPrice(2L, "ETH", 1000.0, start.plusSeconds(3600)),
                new CryptoPrice(3L, "ETH", 1200.0, start.plusSeconds(7200))
        );

        when(cryptoPriceRepository.findByTimestampBetween(start, end)).thenReturn(prices);

        String result = cryptoService.getCryptoWithHighestRangeForDay(date);
        assertEquals("ETH", result); // Only ETH is valid

        verify(cryptoPriceRepository, times(1)).findByTimestampBetween(start, end);
    }
}
