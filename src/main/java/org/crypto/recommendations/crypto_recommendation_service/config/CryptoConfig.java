package org.crypto.recommendations.crypto_recommendation_service.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CryptoConfig {

    @Value("${crypto.symbols.BTC}")
    private String btcFile;

    @Value("${crypto.symbols.DOGE}")
    private String dogeFile;

    @Value("${crypto.symbols.ETH}")
    private String ethFile;

    @Value("${crypto.symbols.LTC}")
    private String ltcFile;

    @Value("${crypto.symbols.XRP}")
    private String xrpFile;

    // You can use a Map to handle dynamic symbols in future as well
    @Getter
    private final Map<String, String> symbols = new HashMap<>();

    @PostConstruct
    public void init() {
        symbols.put("BTC", btcFile);
        symbols.put("DOGE", dogeFile);
        symbols.put("ETH", ethFile);
        symbols.put("LTC", ltcFile);
        symbols.put("XRP", xrpFile);
    }
}
