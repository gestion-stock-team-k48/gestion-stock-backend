package cm.kfokam.stock.auth;

import cm.kfokam.stock.auth.dto.AuthenticationRequest;
import cm.kfokam.stock.auth.dto.AuthenticationResponse;
import cm.kfokam.stock.auth.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(HttpServletRequest request);

    AuthenticationResponse register(RegisterRequest request);
}
