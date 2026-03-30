package id.perumdamts.mail.infrastructure.sqids;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SqidPrefix {
    MAIL_TYPE("mtp"),
    MAIL_CATEGORY("mca"),
    QUICK_MESSAGE("qms");

    private final String prefix;
}
