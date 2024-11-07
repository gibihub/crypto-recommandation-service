package org.crypto.recommendations.crypto_recommendation_service.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.crypto.recommendations.crypto_recommendation_service.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cryptos")
@Tag(name = "Crypto Controller", description = "Manage crypto statistics and recommendations")
public class CryptoController {

    @Autowired
    private CryptoService cryptoService;

    @GetMapping("/{symbol}/stats")
    @Operation(summary = "Get cryptocurrency statistics", description = "This endpoint returns statistical data for a specific cryptocurrency symbol.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cryptocurrency statistics"),
            @ApiResponse(responseCode = "404", description = "Cryptocurrency symbol not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Map<String, Double> getCryptoStatistics(@PathVariable String symbol) {
        return cryptoService.getCryptoStatistics(symbol);
    }

    @GetMapping("/sorted-by-range")
    @Operation(summary = "Get sorted list of cryptocurrencies by normalized range", description = "This endpoint returns a list of cryptocurrency symbols sorted by their normalized range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sorted list of cryptocurrencies"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public List<String> getCryptosSortedByNormalizedRange() {
        return cryptoService.getCryptosSortedByNormalizedRange();
    }

    @GetMapping("/highest-range")
    @Operation(summary = "Get cryptocurrency with the highest range for a specific day", description = "This endpoint returns the cryptocurrency symbol with the highest range for a specific day.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cryptocurrency with the highest range for the specified day"),
            @ApiResponse(responseCode = "400", description = "Invalid date format"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public String getCryptoWithHighestRangeForDay(@RequestParam String date) {
        return cryptoService.getCryptoWithHighestRangeForDay(date);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/load-data/{symbol}")
    @Operation(summary = "Load crypto data for a specific symbol into the database", description = "This endpoint loads cryptocurrency data for a specific symbol from a CSV file into the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Crypto data loaded successfully for the specified symbol"),
            @ApiResponse(responseCode = "400", description = "Invalid cryptocurrency symbol"),
            @ApiResponse(responseCode = "404", description = "CSV file for the specified symbol not found"),
            @ApiResponse(responseCode = "500", description = "Failed to load crypto data")
    })
    public ResponseEntity<String> loadCryptoData(@PathVariable String symbol) {
        try {
            // Delegate to the service layer for processing
            cryptoService.loadCryptoData(symbol);
            return ResponseEntity.ok("Crypto data for symbol " + symbol + " loaded successfully!");
        } catch (IllegalArgumentException e) {
            // Return bad request if symbol is invalid
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (FileNotFoundException e) {
            // Return not found if CSV file is missing
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Return internal server error for any other issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to load crypto data: " + e.getMessage());
        }
    }
}
