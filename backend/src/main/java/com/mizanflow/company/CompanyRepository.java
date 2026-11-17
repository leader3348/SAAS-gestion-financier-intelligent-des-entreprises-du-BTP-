package com.mizanflow.company;
import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface CompanyRepository extends JpaRepository<Company, UUID> {
  /** Verrou de ligne : sérialise la numérotation des factures par entreprise. */
  @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select c from Company c where c.id=:id") Optional<Company> lockById(@Param("id") UUID id); }
