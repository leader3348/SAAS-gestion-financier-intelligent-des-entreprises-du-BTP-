package com.mizanflow.customer;
import com.mizanflow.common.Exceptions; import com.mizanflow.tenant.TenantContext; import jakarta.validation.constraints.*;
import org.springframework.data.domain.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.UUID;
@Service @Transactional
public class CustomerService {
  public record CustomerRequest(@NotBlank @Size(max=160) String name, @Size(max=20) String ice, @Email String email, @Size(max=30) String phone, String address, String city, String notes) {}
  public record CustomerDto(UUID id, String name, String ice, String email, String phone, String address, String city, String notes) {}
  private final CustomerRepository repo; public CustomerService(CustomerRepository r){ repo=r; }
  @Transactional(readOnly=true) public Page<CustomerDto> list(String q, Pageable p){ return repo.search(TenantContext.require(), q==null||q.isBlank()?null:q.trim(), p).map(CustomerService::dto); }
  @Transactional(readOnly=true) public CustomerDto get(UUID id){ return dto(load(id)); }
  public CustomerDto create(CustomerRequest r){ var c = new Customer(); apply(c, r); return dto(repo.save(c)); }
  public CustomerDto update(UUID id, CustomerRequest r){ var c = load(id); apply(c, r); return dto(c); }
  public Customer load(UUID id){ return repo.findByIdAndTenantId(id, TenantContext.require()).orElseThrow(() -> new Exceptions.NotFound("Client introuvable")); }
  private void apply(Customer c, CustomerRequest r){ c.setName(r.name()); c.setIce(r.ice()); c.setEmail(r.email()); c.setPhone(r.phone()); c.setAddress(r.address()); c.setCity(r.city()); c.setNotes(r.notes()); }
  static CustomerDto dto(Customer c){ return new CustomerDto(c.getId(), c.getName(), c.getIce(), c.getEmail(), c.getPhone(), c.getAddress(), c.getCity(), c.getNotes()); }
}
