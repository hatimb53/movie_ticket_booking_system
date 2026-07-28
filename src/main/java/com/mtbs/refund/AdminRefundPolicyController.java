package com.mtbs.refund;

import com.mtbs.refund.domain.RefundPolicy;
import com.mtbs.refund.domain.RefundTier;
import com.mtbs.refund.dto.RefundPolicyDtos.CreateRefundPolicyRequest;
import com.mtbs.refund.dto.RefundPolicyDtos.RefundPolicyResponse;
import com.mtbs.refund.dto.RefundPolicyDtos.TierDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only refund-policy management (theater-level or system default). */
@RestController
@RequestMapping("/admin/refund-policies")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRefundPolicyController {

  private final RefundPolicyAdminService service;

  public AdminRefundPolicyController(RefundPolicyAdminService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<RefundPolicyResponse> upsert(
      @Valid @RequestBody CreateRefundPolicyRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.upsert(request));
  }

  @GetMapping
  public List<RefundPolicyResponse> list() {
    return service.list();
  }

  /** Upsert-by-owner admin operations for refund policies. */
  @Service
  static class RefundPolicyAdminService {

    private final RefundPolicyRepository repository;

    RefundPolicyAdminService(RefundPolicyRepository repository) {
      this.repository = repository;
    }

    @Transactional
    RefundPolicyResponse upsert(CreateRefundPolicyRequest r) {
      List<RefundTier> tiers = r.tiers().stream()
          .map(t -> new RefundTier(t.hoursBeforeShow(), t.refundPercent()))
          .toList();
      RefundPolicy existing = (r.theaterId() == null
          ? repository.findByTheaterIdIsNull()
          : repository.findByTheaterId(r.theaterId())).orElse(null);
      if (existing != null) {
        repository.delete(existing);
        repository.flush();
      }
      RefundPolicy saved = repository.save(new RefundPolicy(r.theaterId(), tiers));
      return toResponse(saved);
    }

    @Transactional(readOnly = true)
    List<RefundPolicyResponse> list() {
      return repository.findAll().stream().map(this::toResponse).toList();
    }

    private RefundPolicyResponse toResponse(RefundPolicy p) {
      List<TierDto> tiers = p.getTiers().stream()
          .map(t -> new TierDto(t.getHoursBeforeShow(), t.getRefundPercent()))
          .toList();
      return new RefundPolicyResponse(p.getId(), p.getTheaterId(), tiers);
    }
  }
}
