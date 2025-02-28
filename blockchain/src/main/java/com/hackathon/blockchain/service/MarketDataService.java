package com.hackathon.blockchain.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class MarketDataService {

    private final RestTemplate restTemplate;
    private static final String API_URL = "https://faas-lon1-917a94a7.doserverless.co/api/v1/web/fn-3d8ede30-848f-4a7a-acc2-22ba0cd9a382/default/fake-market-prices";

    // Precios por defecto por si falla la API
    private static final Map<String, Double> DEFAULT_PRICES = Map.of(
            "BTC", 45000.0,
            "ETH", 3000.0,
            "USDT", 1.0
    );

    public MarketDataService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public double fetchLivePriceForAsset(String symbol) {
        return fetchLiveMarketPrices().getOrDefault(symbol.toUpperCase(), 0.0);
    }

    public Map<String, Double> fetchLiveMarketPrices() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(API_URL, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Double>) response.getBody();
            }

        } catch (Exception e) {
            System.err.println("Error fetching market prices: " + e.getMessage());
        }

        return DEFAULT_PRICES; // Fallback a valores por defecto
    }
}