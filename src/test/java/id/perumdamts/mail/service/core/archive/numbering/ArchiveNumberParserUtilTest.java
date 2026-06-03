package id.perumdamts.mail.service.core.archive.numbering;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArchiveNumberParserUtilTest {

    @Test
    void testParseShortPattern() {
        assertEquals(1524, ArchiveNumberParserUtil.parseSequence("027/1524/2025"));
        assertEquals(635, ArchiveNumberParserUtil.parseSequence("000/I/0635/2026")); // Wait, this has 4 parts, so seq is 0635
        assertEquals(1, ArchiveNumberParserUtil.parseSequence("800/M/0001/2025"));
    }

    @Test
    void testParseLongRomanPattern() {
        assertEquals(406, ArchiveNumberParserUtil.parseSequence("692.1/I/0406/2025"));
        assertEquals(193, ArchiveNumberParserUtil.parseSequence("980.09/I/0193/2025"));
        assertEquals(517, ArchiveNumberParserUtil.parseSequence("973/M/0517/2025"));
    }

    @Test
    void testInvalidPatterns() {
        assertNull(ArchiveNumberParserUtil.parseSequence("INVALID"));
        assertNull(ArchiveNumberParserUtil.parseSequence("123/2025"));
        assertNull(ArchiveNumberParserUtil.parseSequence("027/NOTNUM/2025"));
        assertNull(ArchiveNumberParserUtil.parseSequence(null));
        assertNull(ArchiveNumberParserUtil.parseSequence(""));
    }
}