package com.mizanflow.auth;
import com.mizanflow.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
  private static final String REFRESH_COOKIE = "mizanflow_refresh";
  private final AuthService s;
  private final JwtProperties jwt;
  private final boolean secureCookie;

  public AuthController(AuthService s, JwtProperties jwt, @Value("${mizanflow.auth.refresh-cookie-secure:true}") boolean secureCookie) {
    this.s = s;
    this.jwt = jwt;
    this.secureCookie = secureCookie;
  }

  @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED) AuthDtos.TokenResponse register(@Valid @RequestBody AuthDtos.RegisterRequest r){ return s.register(r); }
  @PostMapping("/login") AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest r){ return s.login(r); }
  @PostMapping("/refresh") AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest r){ return s.refresh(r.refreshToken()); }
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) void logout(@Valid @RequestBody AuthDtos.RefreshRequest r){ s.logout(r.refreshToken()); }

  @PostMapping("/web/register")
  @ResponseStatus(HttpStatus.CREATED)
  AuthDtos.WebTokenResponse registerWeb(@Valid @RequestBody AuthDtos.RegisterRequest r, HttpServletResponse response) {
    var tokens = s.register(r);
    setRefreshCookie(response, tokens.refreshToken(), false);
    return AuthDtos.WebTokenResponse.from(tokens);
  }

  @PostMapping("/web/login")
  AuthDtos.WebTokenResponse loginWeb(@Valid @RequestBody AuthDtos.LoginRequest r, HttpServletResponse response) {
    var tokens = s.login(r);
    setRefreshCookie(response, tokens.refreshToken(), false);
    return AuthDtos.WebTokenResponse.from(tokens);
  }

  @PostMapping("/web/refresh")
  AuthDtos.WebTokenResponse refreshWeb(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken, HttpServletResponse response) {
    if (refreshToken == null || refreshToken.isBlank()) throw new BadCredentialsException("bad");
    var tokens = s.refresh(refreshToken);
    setRefreshCookie(response, tokens.refreshToken(), false);
    return AuthDtos.WebTokenResponse.from(tokens);
  }

  @PostMapping("/web/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void logoutWeb(@CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken, HttpServletResponse response) {
    if (refreshToken != null && !refreshToken.isBlank()) s.logout(refreshToken);
    setRefreshCookie(response, "", true);
  }

  private void setRefreshCookie(HttpServletResponse response, String value, boolean clear) {
    var cookie = ResponseCookie.from(REFRESH_COOKIE, value)
      .httpOnly(true)
      .secure(secureCookie)
      .sameSite("Strict")
      .path("/api/v1/auth")
      .maxAge(clear ? Duration.ZERO : Duration.ofDays(jwt.refreshTtlDays()))
      .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
