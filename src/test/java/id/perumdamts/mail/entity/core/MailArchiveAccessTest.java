package id.perumdamts.mail.entity.core;

import id.perumdamts.mail.util.BooleanYesNoConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MailArchiveAccessTest {

    private final BooleanYesNoConverter converter = new BooleanYesNoConverter();

    @Test
    void testConverterToDatabase() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("Y");
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(" ");
    }

    @Test
    void testConverterToEntity() {
        assertThat(converter.convertToEntityAttribute("Y")).isTrue();
        assertThat(converter.convertToEntityAttribute("y")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
        assertThat(converter.convertToEntityAttribute("n")).isFalse();
        assertThat(converter.convertToEntityAttribute(" ")).isFalse();
        assertThat(converter.convertToEntityAttribute(null)).isFalse();
    }

    @Test
    void testCreateEntity() {
        MailArchiveAccess access = MailArchiveAccess.create(1L, 10, true, false, true);
        assertThat(access.getArchiveId()).isEqualTo(1L);
        assertThat(access.getPositionId()).isEqualTo(10);
        assertThat(access.getCanAccess()).isTrue();
        assertThat(access.getCanDownload()).isFalse();
        assertThat(access.getCanViewHistory()).isTrue();
    }
}
