package com.thinkitdevit.dispatch.client;

import com.thinkitdevit.dispatch.exception.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class StockServiceClient {

    private final RestTemplate restTemplate;

    private final String stockServiceEndpoint;

    public StockServiceClient(RestTemplate restTemplate, @Value("${dispatch.stockServiceEndpoint}") String stockServiceEndpoint) {
        this.restTemplate = restTemplate;
        this.stockServiceEndpoint = stockServiceEndpoint;
    }

    public String checkAvailability(String item){
        try{
            ResponseEntity<String> response = restTemplate.getForEntity(stockServiceEndpoint + "?item=" + item, String.class);
            if (!HttpStatus.OK.equals(response.getStatusCode()) ){
                throw new RuntimeException("Error checking stock availability status code: " + response.getStatusCode());
            }
            return response.getBody();
        }catch (HttpServerErrorException | ResourceAccessException e){
            log.error("Error checking stock availability", e);
            throw new RetryableException(e);
        }catch (Exception e){
            log.error("Exception throw: {}", e.getClass().getName(), e);
            throw e;
        }

    }


}
