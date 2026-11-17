package com.mizanflow;
import com.fasterxml.jackson.databind.*; import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest; import org.springframework.http.MediaType; import org.springframework.test.context.*; import org.springframework.test.web.servlet.*; import org.testcontainers.containers.PostgreSQLContainer; import org.testcontainers.junit.jupiter.*;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
/** Nécessite Docker (Testcontainers + PostgreSQL réel + migrations Flyway). */
@SpringBootTest @AutoConfigureMockMvc @Testcontainers
class ApiIntegrationTest {
  @Container static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");
  @DynamicPropertySource static void props(DynamicPropertyRegistry r){ r.add("spring.datasource.url", pg::getJdbcUrl); r.add("spring.datasource.username", pg::getUsername); r.add("spring.datasource.password", pg::getPassword); r.add("mizanflow.jwt.secret", () -> "test-secret-test-secret-test-secret-123456"); }
  @Autowired MockMvc mvc; static final ObjectMapper om = new ObjectMapper();
  private String register(String company, String email) throws Exception {
    var res = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"companyName\":\""+company+"\",\"fullName\":\"Admin\",\"email\":\""+email+"\",\"password\":\"MotDePasse-Solide1\"}")).andExpect(status().isCreated()).andReturn();
    return om.readTree(res.getResponse().getContentAsString()).get("accessToken").asText(); }
  private String createCustomer(String token, String name) throws Exception {
    var res = mvc.perform(post("/api/v1/customers").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\""+name+"\"}")).andExpect(status().isCreated()).andReturn();
    return om.readTree(res.getResponse().getContentAsString()).get("id").asText(); }
  private Cookie refreshCookie(MvcResult result) {
    String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
    assertNotNull(setCookie);
    String pair = setCookie.split(";", 2)[0];
    int separator = pair.indexOf('=');
    return new Cookie(pair.substring(0, separator), pair.substring(separator + 1));
  }
  @Test void companyCannotReadOrListAnotherCompanysCustomers() throws Exception {
    String a = register("Alpha BTP", "a@alpha.ma"), b = register("Beta Travaux", "b@beta.ma"); String id = createCustomer(a, "Client secret A");
    mvc.perform(get("/api/v1/customers/"+id).header("Authorization","Bearer "+b)).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/customers").header("Authorization","Bearer "+b)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    mvc.perform(put("/api/v1/customers/"+id).header("Authorization","Bearer "+b).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"piraté\"}")).andExpect(status().isNotFound()); }
  @Test void unauthenticatedRequestIsRejected() throws Exception { mvc.perform(get("/api/v1/customers")).andExpect(status().isUnauthorized()); }
  @Test void projectCannotReferenceAnotherTenantsCustomer() throws Exception {
    String a = register("Gamma", "a@gamma.ma"), b = register("Delta", "b@delta.ma"); String foreign = createCustomer(a, "Client Gamma");
    mvc.perform(post("/api/v1/projects").header("Authorization","Bearer "+b).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Villa\",\"customerId\":\""+foreign+"\",\"budget\":1000}")).andExpect(status().isNotFound()); }
  @Test void starterPlanLimitsActiveProjects() throws Exception {
    String t = register("Epsilon", "a@epsilon.ma"); String c = createCustomer(t, "C");
    for (int i = 0; i < 3; i++) mvc.perform(post("/api/v1/projects").header("Authorization","Bearer "+t).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"P"+i+"\",\"customerId\":\""+c+"\",\"budget\":1,\"status\":\"IN_PROGRESS\"}")).andExpect(status().isCreated());
    mvc.perform(post("/api/v1/projects").header("Authorization","Bearer "+t).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"P4\",\"customerId\":\""+c+"\",\"budget\":1,\"status\":\"IN_PROGRESS\"}")).andExpect(status().isPaymentRequired()); }
  @Test void refreshTokenIsRotatedAndReuseIsRejected() throws Exception {
    var res = mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"companyName\":\"Zeta\",\"fullName\":\"A\",\"email\":\"a@zeta.ma\",\"password\":\"MotDePasse-Solide1\"}")).andReturn();
    String rt = om.readTree(res.getResponse().getContentAsString()).get("refreshToken").asText();
    mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+rt+"\"}")).andExpect(status().isOk());
    mvc.perform(post("/api/v1/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\":\""+rt+"\"}")).andExpect(status().isUnauthorized()); }
  @Test void browserSessionKeepsRefreshTokenInHttpOnlyCookie() throws Exception {
    var registered = mvc.perform(post("/api/v1/auth/web/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"companyName\":\"Cookie BTP\",\"fullName\":\"Admin\",\"email\":\"cookie@btp.ma\",\"password\":\"MotDePasse-Solide1\"}"))
      .andExpect(status().isCreated()).andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.refreshToken").doesNotExist())
      .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
      .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
      .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Secure"))).andReturn();
    Cookie cookie = refreshCookie(registered);

    var loggedIn = mvc.perform(post("/api/v1/auth/web/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"cookie@btp.ma\",\"password\":\"MotDePasse-Solide1\"}"))
      .andExpect(status().isOk()).andExpect(jsonPath("$.refreshToken").doesNotExist())
      .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly"))).andReturn();
    Cookie loginCookie = refreshCookie(loggedIn);

    var refreshed = mvc.perform(post("/api/v1/auth/web/refresh").cookie(loginCookie))
      .andExpect(status().isOk()).andExpect(jsonPath("$.refreshToken").doesNotExist())
      .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly"))).andReturn();
    Cookie rotated = refreshCookie(refreshed);
    assertNotEquals(loginCookie.getValue(), rotated.getValue());

    mvc.perform(post("/api/v1/auth/web/logout").cookie(rotated))
      .andExpect(status().isNoContent()).andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
  }
}
