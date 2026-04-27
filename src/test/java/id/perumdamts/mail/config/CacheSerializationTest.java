package id.perumdamts.mail.config;

import id.perumdamts.mail.dto.common.PagedResponse;
import id.perumdamts.mail.dto.core.publication.PublicationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CacheSerializationTest {

    private GenericJacksonJsonRedisSerializer serializer;

    @BeforeEach
    void setUp() {
        PolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("id.perumdamts.mail")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("org.springframework.data.domain")
                .build();

        serializer = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(validator)
                .build();
    }

    @Test
    void shouldSerializeAndDeserializePublicationResponse() {
        PublicationResponse response = new PublicationResponse(
                "pbl_1", "Title", "Desc", null, "PUBLISHED",
                LocalDateTime.now(), "file.pdf", 100, "User", "Title",
                LocalDateTime.now(), LocalDateTime.now()
        );

        byte[] serialized = serializer.serialize(response);
        assertThat(serialized).isNotEmpty();

        Object deserialized = serializer.deserialize(serialized);
        assertThat(deserialized).isInstanceOf(PublicationResponse.class);
        assertThat(deserialized).isEqualTo(response);
    }

    @Test
    void shouldSerializeAndDeserializePagedResponse() {
        PublicationResponse p1 = new PublicationResponse(
                "pbl_1", "Title 1", "Desc 1", null, "PUBLISHED",
                LocalDateTime.now(), "file1.pdf", 100, "User", "Title",
                LocalDateTime.now(), LocalDateTime.now()
        );
        PagedResponse<PublicationResponse> pagedResponse = PagedResponse.of(List.of(p1), 0, 10, 1);

        byte[] serialized = serializer.serialize(pagedResponse);
        assertThat(serialized).isNotEmpty();

        Object deserialized = serializer.deserialize(serialized);
        assertThat(deserialized).isInstanceOf(PagedResponse.class);
        
        PagedResponse<?> result = (PagedResponse<?>) deserialized;
        assert result != null;
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst()).isInstanceOf(PublicationResponse.class);
        assertThat(result.content().getFirst()).isEqualTo(p1);
    }
}
