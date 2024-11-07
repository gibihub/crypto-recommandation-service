package org.crypto.recommendations.crypto_recommendation_service.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.crypto.recommendations.crypto_recommendation_service.repository.CryptoPriceRepository;
import org.crypto.recommendations.crypto_recommendation_service.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cryptos")
@Tag(name = "Crypto Controller", description = "Manage crypto statistics and recommendations")
public class CryptoController {

    @Autowired
    private CryptoService cryptoService;

    @GetMapping("/{symbol}/stats")
    @Operation(
            summary = "Get statistics for a specific cryptocurrency",
            description = "Fetches the statistics (such as price range, etc.) for the given cryptocurrency symbol.",
            parameters = @Parameter(name = "symbol", description = "Symbol of the cryptocurrency (e.g., BTC, ETH)", required = true)
    )
    public Map<String, Double> getCryptoStatistics(@PathVariable String symbol) {
        return cryptoService.getCryptoStatistics(symbol);
    }

    @GetMapping("/sorted-by-range")
    @Operation(
            summary = "Get a list of cryptocurrencies sorted by normalized price range",
            description = "Fetches a list of cryptocurrency symbols sorted by their normalized price range."
    )
    public List<String> getCryptosSortedByNormalizedRange() {
        return cryptoService.getCryptosSortedByNormalizedRange();
    }

    @GetMapping("/highest-range")
    @Operation(
            summary = "Get cryptocurrency with the highest price range for a given day",
            description = "Fetches the cryptocurrency with the highest price range for the specified day.",
            parameters = @Parameter(name = "date", description = "The day (in format yyyy-MM-dd) to fetch the crypto with the highest range", required = true)
    )
    public String getCryptoWithHighestRangeForDay(@RequestParam String date) {
        return cryptoService.getCryptoWithHighestRangeForDay(date);
    }
}
