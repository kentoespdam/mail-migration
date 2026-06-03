package id.perumdamts.mail.service.core.archive.numbering;

public class ArchiveNumberParserUtil {

    /**
     * Parses the sequence number from a legacy archive number.
     * Supports:
     * - SHORT pattern: 027/1524/2025 -> 1524
     * - LONG_ROMAN pattern: 692.1/I/0406/2025 -> 0406
     */
    public static Integer parseSequence(String archiveNumber) {
        if (archiveNumber == null || archiveNumber.isBlank()) {
            return null;
        }

        String[] parts = archiveNumber.split("/");
        if (parts.length < 3) {
            return null; // Invalid pattern
        }

        // Sequence is always the second to last part
        String seqStr = parts[parts.length - 2];
        try {
            return Integer.parseInt(seqStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}