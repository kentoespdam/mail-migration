package id.perumdamts.mail.controller.core;

import id.perumdamts.mail.dto.common.FileDownloadResource;
import id.perumdamts.mail.dto.core.publication.PublicationParams;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import id.perumdamts.mail.entity.core.Publication;
import id.perumdamts.mail.service.core.publication.PublicationCommandService;
import id.perumdamts.mail.service.core.publication.PublicationQueryService;
import id.perumdamts.mail.util.SqidsEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PublicationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublicationCommandService commandService;

    @Mock
    private PublicationQueryService queryService;

    @Mock
    private SqidsEncoder encoder;

    @InjectMocks
    private PublicationController publicationController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(publicationController)
                .build();
    }

    @Test
    void findAll_shouldReturnPagedModel() throws Exception {
        PublicationResponse response = new PublicationResponse(
                "pbl_1", "Title", "Desc", null, "PUBLISHED", null, "file.pdf", 100, "Admin", "Mgr", 1, null, null, 1
        );
        when(queryService.findAll(any(PublicationParams.class))).thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/publications")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("pbl_1"))
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void findById_shouldReturnPublication() throws Exception {
        String id = "pbl_1";
        PublicationResponse response = new PublicationResponse(
                id, "Title", "Desc", null, "PUBLISHED", null, "file.pdf", 100, "Admin", "Mgr", 1, null, null, null
        );
        when(encoder.decode(eq(Publication.class), eq(id))).thenReturn(1L);
        when(queryService.findById(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/v1/publications/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void download_shouldReturnStreamingResponse() throws Exception {
        String id = "pbl_1";
        byte[] content = "test-content".getBytes();
        FileDownloadResource resource = new FileDownloadResource(
                "test.pdf", "application/pdf", new ByteArrayResource(content));
        when(encoder.decode(eq(Publication.class), eq(id))).thenReturn(1L);
        when(queryService.downloadFile(eq(1L))).thenReturn(resource);

        mockMvc.perform(get("/api/v1/publications/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""))
                .andExpect(content().bytes(content));
    }
}
