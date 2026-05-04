package id.perumdamts.mail.controller.master;

import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateRequest;
import id.perumdamts.mail.dto.master.messagetemplate.MessageTemplateResponse;
import id.perumdamts.mail.service.master.MessageTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageTemplateControllerTest {

    @Mock
    private MessageTemplateService service;

    private MessageTemplateController controller;

    @BeforeEach
    void setUp() {
        controller = new MessageTemplateController(service);
    }

    @Test
    void findAll_returnsPage() {
        var response = new MessageTemplateResponse("ID", "Message", "Desc");
        when(service.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(response)));

        var result = controller.findAll(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("ID", result.getContent().iterator().next().getId());
    }

    @Test
    void findById_returnsData() {
        var response = new MessageTemplateResponse("ID", "Message", "Desc");
        when(service.findById("ID")).thenReturn(response);

        var result = controller.findById("ID");

        assertNotNull(result);
        assertEquals("ID", result.getId());
    }

    @Test
    void create_returnsCreated() {
        var request = new MessageTemplateRequest("Msg", "Desc");
        var response = new MessageTemplateResponse("ID", "Msg", "Desc");
        when(service.create(any())).thenReturn(response);

        var result = controller.create(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("ID", result.getBody().getId());
    }

    @Test
    void update_returnsOk() {
        var request = new MessageTemplateRequest("Msg", "Desc");
        var response = new MessageTemplateResponse("ID", "Msg", "Desc");
        when(service.update(eq("ID"), any())).thenReturn(response);

        var result = controller.update("ID", request);

        assertNotNull(result);
        assertEquals("ID", result.getId());
    }

    @Test
    void delete_returnsOk() {
        controller.delete("ID");

        verify(service).delete("ID");
    }
}
