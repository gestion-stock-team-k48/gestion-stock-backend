package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.dto.AuthenticationRequest;
import cm.kfokam.stock.auth.dto.AuthenticationResponse;
import cm.kfokam.stock.auth.dto.RegisterRequest;
import cm.kfokam.stock.entreprise.EntrepriseService;
import cm.kfokam.stock.entreprise.dto.EntrepriseRequest;
import cm.kfokam.stock.entreprise.dto.EntrepriseResponse;
import cm.kfokam.stock.exception.InvalidTokenException;
import cm.kfokam.stock.utilisateur.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
class AuthServiceImpl implements AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final EntrepriseService entrepriseService;
    private final UtilisateurService utilisateurService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }

    @Override
    public AuthenticationResponse refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Refresh token manquant ou invalide");
        }

        String refreshToken = authHeader.substring(BEARER_PREFIX.length());
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new InvalidTokenException("Refresh token invalide");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new InvalidTokenException("Refresh token invalide ou expiré");
        }

        String accessToken = jwtService.generateToken(userDetails);
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        EntrepriseRequest entrepriseRequest = new EntrepriseRequest(
                request.nomEntreprise(), request.description(), request.rue(), request.ville(),
                request.codePostal(), request.pays(), request.codeFiscal(), null,
                request.email(), request.numTel(), request.siteWeb());
        EntrepriseResponse entreprise = entrepriseService.create(entrepriseRequest);

        utilisateurService.createInitialAdmin(entreprise.id(), request.nomAdmin(), request.prenomAdmin(),
                request.emailAdmin(), request.motDePasse(), request.dateDeNaissance());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.emailAdmin());
        String jwtToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken);
    }
}
