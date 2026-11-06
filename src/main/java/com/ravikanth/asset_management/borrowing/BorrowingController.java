
package com.ravikanth.asset_management.borrowing;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BorrowingController {

    private final BorrowingRequestService borrowingRequestService;

    public BorrowingController(
            BorrowingRequestService borrowingRequestService
    ) {
        this.borrowingRequestService = borrowingRequestService;
    }

    // Create a new borrowing request
    @PostMapping("/borrowings")
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowingResponse createBorrowing(
            @Valid @RequestBody CreateBorrowingRequest request,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return borrowingRequestService.createBorrowing(
                userId,
                request.getResourceId()
        );
    }

    // Approve a pending borrowing request
    @PatchMapping("/admin/borrowings/{borrowingId}/approve")
    public BorrowingResponse approveBorrowing(
            @PathVariable Long borrowingId
    ) {
        return borrowingRequestService.approveBorrowing(borrowingId);
    }

    // Reject a pending borrowing request
    @PatchMapping("/admin/borrowings/{borrowingId}/reject")
    public BorrowingResponse rejectBorrowing(
            @PathVariable Long borrowingId
    ) {
        return borrowingRequestService.rejectBorrowing(borrowingId);
    }

    // Return an active borrowing
    @PatchMapping("/borrowings/{borrowingId}/return")
    public BorrowingResponse returnBorrowing(
            @PathVariable Long borrowingId,
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return borrowingRequestService.returnBorrowing(
                borrowingId,
                userId
        );
    }

    // Get borrowing requests belonging to the logged-in user
    @GetMapping("/borrowings/my")
    public List<BorrowingResponse> getMyBorrowings(
            Authentication authentication
    ) {
        Long userId = Long.valueOf(authentication.getName());

        return borrowingRequestService.getBorrowingsForUser(userId)
                .stream()
                .map(BorrowingResponse::from)
                .toList();
    }

    // Get borrowing requests waiting for admin approval
    @GetMapping("/admin/borrowings/pending")
    public List<BorrowingResponse> getPendingBorrowings() {

        return borrowingRequestService.getPendingBorrowings()
                .stream()
                .map(BorrowingResponse::from)
                .toList();
    }

    // Get complete borrowing history for administrators
    @GetMapping("/admin/borrowings")
    public List<BorrowingResponse> getAllBorrowings() {

        return borrowingRequestService.getAllBorrowings()
                .stream()
                .map(BorrowingResponse::from)
                .toList();
    }
}
