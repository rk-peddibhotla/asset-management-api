
package com.ravikanth.asset_management.resource;

import com.ravikanth.asset_management.borrowing.BorrowingRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ResourceCategoryRepository categoryRepository;
    private final BorrowingRequestRepository borrowingRequestRepository;

    public ResourceService(
            ResourceRepository resourceRepository,
            ResourceCategoryRepository categoryRepository,
            BorrowingRequestRepository borrowingRequestRepository
    ) {
        this.resourceRepository = resourceRepository;
        this.categoryRepository = categoryRepository;
        this.borrowingRequestRepository = borrowingRequestRepository;
    }

    // Create a new resource
    @Transactional
    public Resource createResource(
            String name,
            String manufacturer,
            BigDecimal purchasePrice,
            String description,
            Long categoryId
    ) {
        Optional<ResourceCategory> categoryResult =
                categoryRepository.findById(categoryId);

        if (categoryResult.isEmpty()) {
            throw new IllegalArgumentException(
                    "Resource category not found"
            );
        }

        Resource resource = new Resource();

        resource.setName(name);
        resource.setManufacturer(manufacturer);
        resource.setPurchasePrice(purchasePrice);
        resource.setDescription(description);
        resource.setCategory(categoryResult.get());
        resource.setStatus(ResourceStatus.AVAILABLE);

        return resourceRepository.save(resource);
    }

    // Get all resources
    @Transactional(readOnly = true)
    public List<Resource> getAllResources() {
        return resourceRepository.findAllWithCategory();
    }

    // Update resource availability
    @Transactional
    public Resource updateAvailability(
            Long resourceId,
            ResourceStatus status
    ) {
        Resource resource = resourceRepository
                .findByIdForUpdate(resourceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Resource not found"
                ));

        // An actively borrowed resource cannot be marked unavailable.
        if (status == ResourceStatus.UNAVAILABLE
                && borrowingRequestRepository
                        .existsActiveBorrowingForResource(resourceId)) {

            throw new IllegalArgumentException(
                    "Cannot mark a resource unavailable while it is actively borrowed"
            );
        }

        resource.setStatus(status);

        return resourceRepository.save(resource);
    }
}
