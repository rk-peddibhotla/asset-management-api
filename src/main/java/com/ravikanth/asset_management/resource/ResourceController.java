package com.ravikanth.asset_management.resource;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // Create a new resource
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceResponse createResource(
            @Valid @RequestBody CreateResourceRequest request
    ) {

        Resource resource = resourceService.createResource(
                request.getName(),
                request.getManufacturer(),
                request.getPurchasePrice(),
                request.getDescription(),
                request.getCategoryId()
        );

        return ResourceResponse.from(resource);
    }

    // Get all resources
    @GetMapping
    public List<ResourceResponse> getAllResources() {

        List<Resource> resources = resourceService.getAllResources();
        List<ResourceResponse> responses = new ArrayList<>();

        for (Resource resource : resources) {
            responses.add(ResourceResponse.from(resource));
        }

        return responses;
    }

    // Update resource availability
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResourceResponse updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceAvailabilityRequest request
    ) {

        Resource resource = resourceService.updateAvailability(
                id,
                request.status()
        );

        return ResourceResponse.from(resource);
    }
}