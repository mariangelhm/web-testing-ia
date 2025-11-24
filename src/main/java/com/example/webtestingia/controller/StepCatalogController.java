package com.example.webtestingia.controller;

import com.example.webtestingia.model.ApiResponse;
import com.example.webtestingia.service.StepCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes the available test steps discovered in the step definition classes.
 */
@RestController
@RequestMapping("/api/steps")
public class StepCatalogController {

    private final StepCatalogService stepCatalogService;

    public StepCatalogController(StepCatalogService stepCatalogService) {
        this.stepCatalogService = stepCatalogService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listSteps() {
        return ApiResponse.ok(Map.of("steps", stepCatalogService.listStepDefinitions()));
    }
}
