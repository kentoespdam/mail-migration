package id.perumdamts.mail.dto.core.publication;

import org.springframework.core.io.Resource;

public record PublicationDownloadResult(String filename, Resource resource) {}
