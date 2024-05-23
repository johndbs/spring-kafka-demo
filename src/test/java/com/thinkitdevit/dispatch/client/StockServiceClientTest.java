package com.thinkitdevit.dispatch.client;

import com.thinkitdevit.dispatch.exception.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class StockServiceClientTest {

    private static final String ENDPONT_URL = "http://localhost:9001/api/stock";

    private static final String AVAILABILITY_QUERY_URL = ENDPONT_URL+"?item=item1";

    private RestTemplate restTemplate;

    private StockServiceClient stockServiceClient;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        stockServiceClient = new StockServiceClient(restTemplate, ENDPONT_URL);
    }

    @Test
    public void checkAvailability_Success() {
        when(restTemplate.getForEntity(AVAILABILITY_QUERY_URL, String.class))
                .thenReturn(new ResponseEntity<>("true", HttpStatus.OK));

        String checkAvailabiltyReturned = stockServiceClient.checkAvailability("item1");

        assertThat(checkAvailabiltyReturned, equalTo("true"));
        verify(restTemplate, times(1)).getForEntity(AVAILABILITY_QUERY_URL, String.class);
    }

    @Test
    public void checkAvailability_ResourceAccessException() {
        doThrow(new ResourceAccessException("Access error")).when(restTemplate).getForEntity(AVAILABILITY_QUERY_URL, String.class);

        assertThrows(RetryableException.class, () -> stockServiceClient.checkAvailability("item1"));


        verify(restTemplate, times(1)).getForEntity(AVAILABILITY_QUERY_URL, String.class);
    }

    @Test
    public void checkAvailability_RuntimeException() {
        doThrow(new RuntimeException("Other exception")).when(restTemplate).getForEntity(AVAILABILITY_QUERY_URL, String.class);

        assertThrows(RuntimeException.class, () -> stockServiceClient.checkAvailability("item1"));


        verify(restTemplate, times(1)).getForEntity(AVAILABILITY_QUERY_URL, String.class);
    }

}
