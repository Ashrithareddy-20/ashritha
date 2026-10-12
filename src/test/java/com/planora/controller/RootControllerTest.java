package com.planora.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RootControllerTest {

    private RootController rootController;

    @BeforeEach
    void setUp() {
        rootController = new RootController();
    }

    @Test
    void root_ShouldReturn200AndMessage() {
        ResponseEntity<Map<String, String>> response = rootController.root();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Planora backend is running", response.getBody().get("message"));
    }
}
