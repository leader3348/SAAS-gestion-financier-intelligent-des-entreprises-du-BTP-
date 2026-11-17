package com.mizanflow.project;
import jakarta.validation.Valid; import org.springframework.data.domain.*; import org.springframework.http.HttpStatus; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.UUID;
@RestController @RequestMapping("/api/v1/projects")
public class ProjectController { private final ProjectService s; public ProjectController(ProjectService s){ this.s=s; }
  @GetMapping @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT','FIELD_WORKER')")
  Page<ProjectService.ProjectDto> list(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size){ return s.list(PageRequest.of(page, Math.min(size,100), Sort.by("createdAt").descending())); }
  @GetMapping("/{id}") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER','ACCOUNTANT','FIELD_WORKER')") ProjectService.ProjectDto get(@PathVariable UUID id){ return s.get(id); }
  @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER')")
  ProjectService.ProjectDto create(@Valid @RequestBody ProjectService.ProjectRequest r){ return s.create(r); }
  @PutMapping("/{id}") @PreAuthorize("hasAnyRole('COMPANY_ADMIN','PROJECT_MANAGER')")
  ProjectService.ProjectDto update(@PathVariable UUID id, @Valid @RequestBody ProjectService.ProjectRequest r){ return s.update(id, r); } }
