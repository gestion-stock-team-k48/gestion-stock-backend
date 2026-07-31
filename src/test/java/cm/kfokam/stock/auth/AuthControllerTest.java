package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.dto.AuthenticationRequest;
import cm.kfokam.stock.auth.dto.AuthenticationResponse;
import cm.kfokam.stock.auth.dto.RegisterRequest;
import cm.kfokam.stock.exception.DuplicateCodeException;
import cm.kfokam.stock.exception.InvalidTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void authenticate_shouldReturn200_whenValidRequest() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("francky@kfokam.cm", "P@ssw0rd!");
        AuthenticationResponse response = new AuthenticationResponse("access-token", "refresh-token");

        when(authService.authenticate(request)).thenReturn(response);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void authenticate_shouldReturn400_whenEmailIsBlank() throws Exception {
        AuthenticationRequest invalidRequest = new AuthenticationRequest("", "P@ssw0rd!");

        mockMvc.perform(post("/auth/authenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticate_shouldReturn401_whenBadCredentials() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest("francky@kfokam.cm", "wrong-password");

        when(authService.authenticate(request)).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/authenticate")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturn200_whenValid() throws Exception {
        AuthenticationResponse response = new AuthenticationResponse("new-access-token", "refresh-token");
        when(authService.refreshToken(any(HttpServletRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", "Bearer refresh-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"));
    }

    @Test
    void refreshToken_shouldReturn401_whenTokenInvalid() throws Exception {
        when(authService.refreshToken(any(HttpServletRequest.class)))
                .thenThrow(new InvalidTokenException("Refresh token invalide ou expiré"));

        mockMvc.perform(post("/auth/refresh-token")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private RegisterRequest validRegisterRequest() {
        return new RegisterRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm",
                "Tchana", "Francky", "francky@kfokam.cm", "P@ssw0rd!", LocalDate.of(1995, 3, 10)
        );
    }

    @Test
    void register_shouldReturn201_whenValidRequest() throws Exception {
        RegisterRequest request = validRegisterRequest();
        AuthenticationResponse response = new AuthenticationResponse("access-token", "refresh-token");

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void register_shouldReturn409_whenCodeFiscalAlreadyUsed() throws Exception {
        RegisterRequest request = validRegisterRequest();

        when(authService.register(request))
                .thenThrow(new DuplicateCodeException("Le code fiscal 'CF-001' est déjà utilisé"));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
