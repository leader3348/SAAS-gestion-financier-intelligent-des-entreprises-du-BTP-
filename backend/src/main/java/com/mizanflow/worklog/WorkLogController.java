package com.mizanflow.worklog;
import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/v1/work-logs")
public class WorkLogController { private final WorkLogService s; public WorkLogController(WorkLogService s){ this.s=s; }
  public record Comment(String comment) {}
  @GetMapping @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT','FIELD_WORKER')")
  Page<WorkLogService.WorkLogDto> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size){ return s.list(PageRequest.of(page, Math.min(size,100), Sort.by("workDate").descending())); }
  @GetMapping("/summary") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT','FIELD_WORKER')") Map<String,Long> summary(){ return s.summary(); }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','FIELD_WORKER')") WorkLogService.WorkLogDto create(@Valid @RequestBody WorkLogService.WorkLogRequest r){ return s.create(r); }
  @PutMapping("/{id}") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','FIELD_WORKER')") WorkLogService.WorkLogDto update(@PathVariable UUID id, @Valid @RequestBody WorkLogService.WorkLogRequest r){ return s.update(id, r); }
  @PostMapping("/{id}/submit") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','FIELD_WORKER')") WorkLogService.WorkLogDto submit(@PathVariable UUID id){ return s.submit(id); }
  @PostMapping("/{id}/approve") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER')") WorkLogService.WorkLogDto approve(@PathVariable UUID id, @RequestBody(required=false) Comment c){ return s.approve(id, c==null?null:c.comment()); }
  @PostMapping("/{id}/reject") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER')") WorkLogService.WorkLogDto reject(@PathVariable UUID id, @RequestBody Comment c){ return s.reject(id, c==null?null:c.comment()); } }
