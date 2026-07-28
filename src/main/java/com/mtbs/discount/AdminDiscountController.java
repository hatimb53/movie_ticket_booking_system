package com.mtbs.discount;

import com.mtbs.discount.domain.DiscountCode;
import com.mtbs.discount.dto.DiscountDtos.CreateDiscountRequest;
import com.mtbs.discount.dto.DiscountDtos.DiscountResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only discount code management. */
@RestController
@RequestMapping("/admin/discounts")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDiscountController {

  private final DiscountAdminService service;

  public AdminDiscountController(DiscountAdminService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<DiscountResponse> create(@Valid @RequestBody CreateDiscountRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
  }

  @GetMapping
  public List<DiscountResponse> list() {
    return service.list();
  }

  @PostMapping("/{id}/deactivate")
  public DiscountResponse deactivate(@PathVariable Long id) {
    return service.setActive(id, false);
  }

  /** Admin write operations for discounts, kept beside the controller for cohesion. */
  @Service
  static class DiscountAdminService {

    private final DiscountCodeRepository repository;

    DiscountAdminService(DiscountCodeRepository repository) {
      this.repository = repository;
    }

    @Transactional
    DiscountResponse create(CreateDiscountRequest r) {
      if (repository.existsByCode(r.code())) {
        throw new DiscountNotApplicableException("Discount code already exists: " + r.code());
      }
      DiscountCode dc = repository.save(new DiscountCode(
          r.code(), r.type(), r.value(), r.maxDiscountAmount(), r.minBookingAmount(),
          r.validFrom(), r.validUntil(), r.usageLimit(), r.active()));
      return toResponse(dc);
    }

    @Transactional(readOnly = true)
    List<DiscountResponse> list() {
      return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    DiscountResponse setActive(Long id, boolean active) {
      DiscountCode dc = repository.findById(id)
          .orElseThrow(() ->
              new com.mtbs.common.error.ResourceNotFoundException("Discount " + id + " not found"));
      dc.setActive(active);
      return toResponse(dc);
    }

    private DiscountResponse toResponse(DiscountCode d) {
      return new DiscountResponse(
          d.getId(), d.getCode(), d.getType().name(), d.getValue(), d.getMaxDiscountAmount(),
          d.getMinBookingAmount(), d.getValidFrom(), d.getValidUntil(), d.getUsageLimit(),
          d.getUsedCount(), d.isActive());
    }
  }
}
