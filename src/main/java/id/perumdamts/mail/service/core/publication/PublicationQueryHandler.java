package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.PublicationDownloadResult;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;

public interface PublicationQueryHandler {
    PagedResponse<PublicationResponse> findAll(PublicationParams params);

    PublicationResponse findById(Long id);

    PublicationDownloadResult download(Long id);
}

