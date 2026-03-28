package id.perumdamts.mail.security;

/**
 * Dilempar saat token AppWrite tidak valid, expired, atau tidak ada.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
