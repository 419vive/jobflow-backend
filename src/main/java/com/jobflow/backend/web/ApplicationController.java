package com.jobflow.backend.web;

import com.jobflow.backend.application.ApplicationService;
import com.jobflow.backend.application.ApplicationSummary;
import com.jobflow.backend.application.ApplicationView;
import com.jobflow.backend.domain.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    ResponseEntity<ApplicationView> create(
            @Valid @RequestBody CreateApplicationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, idempotencyKey));
    }

    @GetMapping
    Page<ApplicationView> search(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Boolean remote,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return service.search(status, remote, pageable);
    }

    @GetMapping("/{id}")
    ApplicationView get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    ApplicationView changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request
    ) {
        return service.changeStatus(id, request.getStatus());
    }

    @GetMapping("/summary")
    ApplicationSummary summary() {
        return service.summary();
    }
}
