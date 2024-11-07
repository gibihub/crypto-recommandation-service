package org.crypto.recommendations.crypto_recommendation_service.util;


import org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class CSVLoader {

    public List<org.crypto.recommendations.crypto_recommendation_service.model.CryptoPrice> loadPricesFromCSV(String symbol) {
        List<CryptoPrice> prices = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("data/" + symbol + "_values.csv")))) {

            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                long timestamp = Long.parseLong(fields[0]);
                double price = Double.parseDouble(fields[2]);

                CryptoPrice priceData = new CryptoPrice();
                priceData.setSymbol(symbol);
                priceData.setPrice(price);
                priceData.setTimestamp(Instant.ofEpochMilli(timestamp));

                prices.add(priceData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prices;
    }
}
