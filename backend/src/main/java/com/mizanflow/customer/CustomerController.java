package com.mizanflow.customer;
import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/customers") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT')")
public class CustomerController { private final CustomerService s; public CustomerController(CustomerService s){ this.s=s; }
  @GetMapping Page<CustomerService.CustomerDto> list(@RequestParam(required=false) String q, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size){
    return s.list(q, PageRequest.of(page, Math.min(size, 100), Sort.by("name"))); }
  @GetMapping("/{id}") CustomerService.CustomerDto get(@PathVariable UUID id){ return s.get(id); }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) CustomerService.CustomerDto create(@Valid @RequestBody CustomerService.CustomerRequest r){ return s.create(r); }
  @PutMapping("/{id}") CustomerService.CustomerDto update(@PathVariable UUID id, @Valid @RequestBody CustomerService.CustomerRequest r){ return s.update(id, r); } }
