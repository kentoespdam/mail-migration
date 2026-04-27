package id.perumdamts.mail.service.core.publication;

import id.perumdamts.mail.dto.core.publication.CreatePublicationRequest;
import id.perumdamts.mail.dto.core.publication.PublicationCommandResult;
import id.perumdamts.mail.dto.core.publication.UpdatePublicationRequest;
import id.perumdamts.mail.security.MailPrincipal;

public interface PublicationCommandHandler {
    PublicationCommandResult create(CreatePublicationRequest request, MailPrincipal principal);

    PublicationCommandResult update(Long id, UpdatePublicationRequest request, MailPrincipal principal);

    void delete(Long id, MailPrincipal principal);

    PublicationCommandResult publish(Long id, MailPrincipal principal);
}
