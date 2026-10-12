package com.planora.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Tag(name = "Root", description = "Root health endpoint for deployment verification")
public class RootController {

    @GetMapping({"/", ""})
    @Operation(summary = "Root health check", description = "Confirms that the Planora backend is running")
    @ApiResponse(responseCode = "200", description = "Planora backend is running successfully")
    public ResponseEntity<Map<String, String>> root() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("message", "Planora backend is running");
        return ResponseEntity.ok(response);
    }
}
