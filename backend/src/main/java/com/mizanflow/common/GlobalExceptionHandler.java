package com.mizanflow.common;
import org.slf4j.*; import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  public record ApiError(Instant timestamp, int status, String code, String message, Map<String,String> fields) {}
  private ResponseEntity<ApiError> r(HttpStatus s,String code,String m,Map<String,String> f){ return ResponseEntity.status(s).body(new ApiError(Instant.now(),s.value(),code,m,f)); }
  @ExceptionHandler(Exceptions.NotFound.class) ResponseEntity<ApiError> nf(Exceptions.NotFound e){ return r(HttpStatus.NOT_FOUND,"NOT_FOUND",e.getMessage(),null); }
  @ExceptionHandler(Exceptions.BusinessRule.class) ResponseEntity<ApiError> br(Exceptions.BusinessRule e){ return r(HttpStatus.UNPROCESSABLE_ENTITY,"BUSINESS_RULE",e.getMessage(),null); }
  @ExceptionHandler(Exceptions.QuotaExceeded.class) ResponseEntity<ApiError> q(Exceptions.QuotaExceeded e){ return r(HttpStatus.PAYMENT_REQUIRED,"QUOTA_EXCEEDED",e.getMessage(),null); }
  @ExceptionHandler({Exceptions.Forbidden.class, AccessDeniedException.class}) ResponseEntity<ApiError> fb(Exception e){ return r(HttpStatus.FORBIDDEN,"FORBIDDEN","Accès refusé",null); }
  @ExceptionHandler(BadCredentialsException.class) ResponseEntity<ApiError> bc(Exception e){ return r(HttpStatus.UNAUTHORIZED,"BAD_CREDENTIALS","Identifiants invalides",null); }
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiError> v(MethodArgumentNotValidException e){
    Map<String,String> f=new LinkedHashMap<>(); e.getBindingResult().getFieldErrors().forEach(x->f.put(x.getField(),x.getDefaultMessage()));
    return r(HttpStatus.BAD_REQUEST,"VALIDATION","Données invalides",f); }
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> any(Exception e){ log.error("Unhandled",e); return r(HttpStatus.INTERNAL_SERVER_ERROR,"INTERNAL","Erreur interne",null); }
}
