package id.perumdamts.mail.repository.core.jooq;

import id.perumdamts.mail.dto.core.attachment.AttachmentResponse;
import id.perumdamts.mail.dto.id.AttachmentId;
import id.perumdamts.mail.dto.id.MailId;
import id.perumdamts.mail.enums.AttachmentRefType;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Repository
@RequiredArgsConstructor
public class AttachmentQueryRepository {

    private final DSLContext dsl;

    public List<AttachmentResponse> findByRef(AttachmentRefType refType, Long refId) {
        return dsl.select()
                .from(table("attachments"))
                .where(field("ref_type").eq(refType.getDbValue()))
                .and(field("ref_id").eq(refId))
                .and(field("status").eq(1))
                .fetch(this::toResponse);
    }

    public Optional<AttachmentResponse> findById(long id) {
        return dsl.select()
                .from(table("attachments"))
                .where(field("id").eq(id))
                .and(field("status").eq(1))
                .fetchOptional(this::toResponse);
    }

    private AttachmentResponse toResponse(Record r) {
        Long id = r.get(field("id"), Long.class);
        Integer refType = r.get(field("ref_type"), Integer.class);
        Long refId = r.get(field("ref_id"), Long.class);

        return new AttachmentResponse(
                id != null ? new AttachmentId(id) : null,
                refType,
                (refType != null && refType == AttachmentRefType.MAIL.getDbValue() && refId != null)
                        ? new MailId(refId).toString()
                        : (refId != null ? String.valueOf(refId) : null),
                r.get(field("original_filename"), String.class),
                r.get(field("file_ext"), String.class),
                r.get(field("file_size"), Integer.class),
                r.get(field("doc_notes"), String.class),
                r.get(field("upload_date"), LocalDateTime.class),
                r.get(field("upload_by_name"), String.class)
        );
    }
}
