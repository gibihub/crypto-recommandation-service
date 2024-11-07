package org.crypto.recommendations.crypto_recommendation_service.service;

import org.crypto.recommendations.crypto_recommendation_service.config.CryptoConfig;
import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.crypto.recommendations.crypto_recommendation_service.repository.CryptoPriceRepository;
import org.crypto.recommendations.crypto_recommendation_service.util.CSVLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CryptoService {


    private CryptoConfig cryptoConfig;

    private CryptoPriceRepository cryptoPriceRepository;

    private CSVLoader csvLoader;

    @Autowired
    public CryptoService(CryptoConfig cryptoConfig, CryptoPriceRepository cryptoPriceRepository, CSVLoader csvLoader) {
        this.cryptoConfig = cryptoConfig;
        this.cryptoPriceRepository = cryptoPriceRepository;
        this.csvLoader = csvLoader;
    }

    @Transactional
    public void loadCryptoData(String symbol) throws IllegalArgumentException, FileNotFoundException {
        // Dynamically get the valid symbol from the config
        String fileName = cryptoConfig.getSymbols().get(symbol);

        if (fileName == null) {
            throw new IllegalArgumentException("Invalid cryptocurrency symbol: " + symbol + ". Valid symbols are: " + String.join(", ", cryptoConfig.getSymbols().keySet()));
        }

        // Load data from CSV using the file name mapped to the symbol
        List<CryptoPrice> prices = csvLoader.loadPricesFromCSV(fileName);
        if (prices == null || prices.isEmpty()) {
            throw new FileNotFoundException("CSV file for the symbol " + symbol + " not found.");
        }
        cryptoPriceRepository.saveAll(prices);
    }

    public Map<String, Double> getCryptoStatistics(String symbol) {
        List<CryptoPrice> prices = cryptoPriceRepository.findBySymbol(symbol);
        double min = prices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double max = prices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        Instant oldest = prices.stream().map(CryptoPrice::getTimestamp).min(Instant::compareTo).orElse(null);
        Instant newest = prices.stream().map(CryptoPrice::getTimestamp).max(Instant::compareTo).orElse(null);

        Map<String, Double> stats = new HashMap<>();
        stats.put("min", min);
        stats.put("max", max);
        stats.put("oldest", oldest != null ? (double) oldest.getEpochSecond() : 0);
        stats.put("newest", newest != null ? (double) newest.getEpochSecond() : 0);

        return stats;
    }

    public List<String> getCryptosSortedByNormalizedRange() {
        List<String> symbols = cryptoPriceRepository.findAll()
                .stream()
                .map(CryptoPrice::getSymbol)
                .distinct()
                .collect(Collectors.toList());

        return symbols.stream()
                .sorted((s1, s2) -> Double.compare(calculateNormalizedRange(s2), calculateNormalizedRange(s1)))
                .collect(Collectors.toList());
    }

    public String getCryptoWithHighestRangeForDay(String date) {
        // Convert Instant to LocalDate at UTC
        Instant day = Instant.parse(date);
        LocalDate localDate = day.atZone(ZoneOffset.UTC).toLocalDate();

        // Get start and end of the day
        Instant start = localDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = start.plus(1, ChronoUnit.DAYS);

        return cryptoPriceRepository.findByTimestampBetween(start, end).stream()
                .collect(Collectors.groupingBy(CryptoPrice::getSymbol, Collectors.summarizingDouble(CryptoPrice::getPrice)))
                .entrySet().stream()
                .max(Comparator.comparingDouble(e -> (e.getValue().getMax() - e.getValue().getMin()) / e.getValue().getMin()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private double calculateNormalizedRange(String symbol) {
        List<CryptoPrice> prices = cryptoPriceRepository.findBySymbol(symbol);
        if (prices.isEmpty()) return 0;

        double min = prices.stream().mapToDouble(CryptoPrice::getPrice).min().orElse(0);
        double max = prices.stream().mapToDouble(CryptoPrice::getPrice).max().orElse(0);
        return (max - min) / min;
    }
}
