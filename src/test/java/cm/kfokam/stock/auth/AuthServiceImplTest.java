package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.dto.AuthenticationRequest;
import cm.kfokam.stock.auth.dto.AuthenticationResponse;
import cm.kfokam.stock.auth.dto.RegisterRequest;
import cm.kfokam.stock.entreprise.EntrepriseService;
import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.exception.InvalidTokenException;
import cm.kfokam.stock.utilisateur.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private EntrepriseService entrepriseService;

    @Mock
    private UtilisateurService utilisateurService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthenticationRequest request;

    @BeforeEach
    void setUp() {
        request = new AuthenticationRequest("francky@kfokam.cm", "P@ssw0rd!");
    }

    @Test
    void register_shouldCreateEntrepriseAndInitialAdmin_thenReturnTokens() {
        RegisterRequest registerRequest = new RegisterRequest(
                "Kfokam SARL", "Gestion de stock", null, "Douala", null, "Cameroun",
                "CF-001", "contact@kfokam.cm", "+237600000000", "https://kfokam.cm",
                "Tchana", "Francky", "francky@kfokam.cm", "P@ssw0rd!", LocalDate.of(1995, 3, 10)
        );
        EntrepriseResponse entrepriseResponse = new EntrepriseResponse(1L, "Kfokam SARL", "Gestion de stock",
                null, "Douala", null, "Cameroun", "CF-001", null, "contact@kfokam.cm", "+237600000000", "https://kfokam.cm");

        when(entrepriseService.create(any(EntrepriseRequest.class))).thenReturn(entrepriseResponse);
        when(userDetailsService.loadUserByUsername("francky@kfokam.cm")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthenticationResponse result = authService.register(registerRequest);

        assertThat(result).isEqualTo(new AuthenticationResponse("access-token", "refresh-token"));
        verify(utilisateurService).createInitialAdmin(1L, "Tchana", "Francky", "francky@kfokam.cm",
                "P@ssw0rd!", LocalDate.of(1995, 3, 10));
    }

    @Test
    void authenticate_shouldReturnTokens_whenCredentialsValid() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthenticationResponse result = authService.authenticate(request);

        assertThat(result).isEqualTo(new AuthenticationResponse("access-token", "refresh-token"));
    }

    @Test
    void authenticate_shouldPropagateBadCredentialsException_whenInvalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenValid() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer old-refresh-token");
        when(jwtService.extractUsername("old-refresh-token")).thenReturn("francky@kfokam.cm");
        when(userDetailsService.loadUserByUsername("francky@kfokam.cm")).thenReturn(userDetails);
        when(jwtService.isTokenValid("old-refresh-token", userDetails)).thenReturn(true);
        when(jwtService.generateToken(userDetails)).thenReturn("new-access-token");

        AuthenticationResponse result = authService.refreshToken(httpServletRequest);

        assertThat(result).isEqualTo(new AuthenticationResponse("new-access-token", "old-refresh-token"));
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenException_whenHeaderMissing() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest))
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenException_whenHeaderNotBearer() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest))
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void refreshToken_shouldThrowInvalidTokenException_whenTokenInvalid() {
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer old-refresh-token");
        when(jwtService.extractUsername("old-refresh-token")).thenReturn("francky@kfokam.cm");
        when(userDetailsService.loadUserByUsername("francky@kfokam.cm")).thenReturn(userDetails);
        when(jwtService.isTokenValid("old-refresh-token", userDetails)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(httpServletRequest))
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
