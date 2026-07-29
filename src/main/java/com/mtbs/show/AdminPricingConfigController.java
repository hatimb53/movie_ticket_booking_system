package com.mtbs.show;

import com.mtbs.show.dto.PricingConfigDtos.PricingConfigResponse;
import com.mtbs.show.dto.PricingConfigDtos.UpdatePricingConfigRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only pricing-tier configuration (currently: the weekend surcharge multiplier). */
@RestController
@RequestMapping("/admin/pricing-config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingConfigController {

  private final PricingConfigService pricingConfigService;

  public AdminPricingConfigController(PricingConfigService pricingConfigService) {
    this.pricingConfigService = pricingConfigService;
  }

  @GetMapping
  public PricingConfigResponse get() {
    return pricingConfigService.get();
  }

  @PutMapping
  public PricingConfigResponse update(@Valid @RequestBody UpdatePricingConfigRequest request) {
    return pricingConfigService.updateWeekendMultiplier(request.weekendMultiplier());
  }
}
