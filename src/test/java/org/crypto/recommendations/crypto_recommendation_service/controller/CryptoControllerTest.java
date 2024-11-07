package org.crypto.recommendations.crypto_recommendation_service.controller;

import org.crypto.recommendations.crypto_recommendation_service.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class CryptoControllerTest {

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private CryptoController cryptoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCryptoStatistics() {
        // Arrange
        Map<String, Double> mockStats = Map.of("price", 50000.0, "volume", 3000000.0);
        when(cryptoService.getCryptoStatistics("BTC")).thenReturn(mockStats);

        // Act
        Map<String, Double> response = cryptoController.getCryptoStatistics("BTC");

        // Assert
        assertEquals(mockStats, response);
    }

    @Test
    void testGetCryptosSortedByNormalizedRange() {
        // Arrange
        List<String> sortedSymbols = List.of("BTC", "ETH", "DOGE");
        when(cryptoService.getCryptosSortedByNormalizedRange()).thenReturn(sortedSymbols);

        // Act
        List<String> response = cryptoController.getCryptosSortedByNormalizedRange();

        // Assert
        assertEquals(sortedSymbols, response);
    }

    @Test
    void testGetCryptoWithHighestRangeForDay() {
        // Arrange
        String expectedSymbol = "BTC";
        when(cryptoService.getCryptoWithHighestRangeForDay("2023-10-05")).thenReturn(expectedSymbol);

        // Act
        String response = cryptoController.getCryptoWithHighestRangeForDay("2023-10-05");

        // Assert
        assertEquals(expectedSymbol, response);
    }

    @Test
    void testLoadCryptoDataSuccess() {
        // Act
        ResponseEntity<String> response = cryptoController.loadCryptoData("BTC");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Crypto data for symbol BTC loaded successfully!", response.getBody());
    }

    @Test
    void testLoadCryptoDataInvalidSymbol() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid symbol")).when(cryptoService).loadCryptoData(anyString());

        // Act
        ResponseEntity<String> response = cryptoController.loadCryptoData("INVALID");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid symbol", response.getBody());
    }

    @Test
    void testLoadCryptoDataFileNotFound() {
        // Arrange
        doThrow(new RuntimeException("CSV file not found")).when(cryptoService).loadCryptoData(anyString());

        // Act
        ResponseEntity<String> response = cryptoController.loadCryptoData("UNKNOWN");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to load crypto data: CSV file not found", response.getBody());
    }

}
