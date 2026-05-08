package id.perumdamts.mail.service.core.mail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MailSubjectPrefixTest {

    @Test
    void prefixSubjectIfChild_withParentMail_addsFwdPrefix() throws Exception {
        String result = callPrefixSubject("Original Subject", "root123", "parent456");
        assertThat(result).isEqualTo("Fwd: Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withNoParentMail_returnsOriginal() throws Exception {
        String result = callPrefixSubject("Original Subject", "root123", null);
        assertThat(result).isEqualTo("Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withBlankParentMail_returnsOriginal() throws Exception {
        String result = callPrefixSubject("Original Subject", "root123", "");
        assertThat(result).isEqualTo("Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withNoRootMail_returnsOriginal() throws Exception {
        String result = callPrefixSubject("Original Subject", null, "parent456");
        assertThat(result).isEqualTo("Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withExistingFwdPrefix_doesNotDuplicate() throws Exception {
        String result = callPrefixSubject("Fwd: Original Subject", "root123", "parent456");
        assertThat(result).isEqualTo("Fwd: Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withLowercaseFwd_doesNotDuplicate() throws Exception {
        String result = callPrefixSubject("fwd: Original Subject", "root123", "parent456");
        assertThat(result).isEqualTo("fwd: Original Subject");
    }

    @Test
    void prefixSubjectIfChild_withNullSubject_handlesGracefully() throws Exception {
        String result = callPrefixSubject(null, "root123", "parent456");
        assertThat(result).isEqualTo("Fwd: null");
    }

    private String callPrefixSubject(String subject, String rootMailSqid, String parentMailSqid) throws Exception {
        Method method = MailCommandService.class.getDeclaredMethod(
                "prefixSubjectIfChild", String.class, String.class, String.class);
        method.setAccessible(true);
        MailCommandService service = new MailCommandService(
                null, null, null, null, null, null, null, null, null, null, null);
        return (String) method.invoke(service, subject, rootMailSqid, parentMailSqid);
    }
}