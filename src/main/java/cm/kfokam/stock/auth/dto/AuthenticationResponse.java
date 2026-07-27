package cm.kfokam.stock.auth.dto;

public record AuthenticationResponse(
        String token,
        String refreshToken
) {
}
