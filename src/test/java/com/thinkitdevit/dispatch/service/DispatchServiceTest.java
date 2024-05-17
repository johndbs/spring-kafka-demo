package com.thinkitdevit.dispatch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispatchServiceTest {

    private DispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new DispatchService();
    }

    @Test
    void process() {
        dispatchService.process("payload");
    }
}