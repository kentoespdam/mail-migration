package id.perumdamts.mail.infrastructure.security;

/**
 * Dilempar saat token AppWrite tidak valid, expired, atau tidak ada.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
