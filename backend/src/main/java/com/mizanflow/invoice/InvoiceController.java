package com.mizanflow.invoice;
import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','ACCOUNTANT')")
public class InvoiceController { private final InvoiceService s; public InvoiceController(InvoiceService s){ this.s=s; }
  public record IssueRequest(int paymentDays) {} public record CancelRequest(String reason) {}
  @GetMapping("/invoices") Page<InvoiceService.InvoiceDto> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size){ return s.list(PageRequest.of(page, Math.min(size,100), Sort.by("createdAt").descending())); }
  @GetMapping("/invoices/{id}") InvoiceService.InvoiceDto get(@PathVariable UUID id){ return s.get(id); }
  @PostMapping("/invoices/from-work-logs") @ResponseStatus(HttpStatus.CREATED) InvoiceService.InvoiceDto create(@Valid @RequestBody InvoiceService.FromWorkLogsRequest r){ return s.createFromWorkLogs(r); }
  @PostMapping("/invoices/{id}/issue") InvoiceService.InvoiceDto issue(@PathVariable UUID id, @RequestBody IssueRequest r){ return s.issue(id, Math.max(0, r.paymentDays())); }
  @PostMapping("/invoices/{id}/cancel") InvoiceService.InvoiceDto cancel(@PathVariable UUID id, @RequestBody CancelRequest r){ return s.cancel(id, r.reason()); }
  @PostMapping("/invoices/{id}/payments") @ResponseStatus(HttpStatus.CREATED) InvoiceService.PaymentDto pay(@PathVariable UUID id, @Valid @RequestBody InvoiceService.PaymentRequest r){ return s.recordPayment(id, r); }
  @GetMapping("/payments") Page<InvoiceService.PaymentDto> payments(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size){ return s.payments(PageRequest.of(page, Math.min(size,100), Sort.by("paidOn").descending())); } }
