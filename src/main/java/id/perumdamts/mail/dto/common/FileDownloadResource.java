package id.perumdamts.mail.dto.common;

import org.springframework.core.io.Resource;

public record FileDownloadResource(String fileName, String contentType, Resource resource) {}
