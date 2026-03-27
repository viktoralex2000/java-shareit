package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeRequestDtoWithItems() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need drill")
                .requesterId(2L)
                .created(LocalDateTime.of(2026, 3, 27, 12, 0))
                .items(List.of(ItemDto.builder()
                        .id(10L)
                        .name("Drill")
                        .ownerId(5L)
                        .build()))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need drill");
        assertThat(result).hasJsonPath("$.items[0].id");
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
    }

    @Test
    void shouldDeserializeRequestDto() throws Exception {
        String content = "{"
                + "  \"id\": 1,"
                + "  \"description\": \"Need drill\","
                + "  \"requesterId\": 2,"
                + "  \"created\": \"2026-03-27T12:00:00\""
                + "}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need drill");
        assertThat(dto.getRequesterId()).isEqualTo(2L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 3, 27, 12, 0));
    }
}