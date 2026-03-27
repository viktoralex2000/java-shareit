package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void shouldSerializeItemDtoWithBookingsAndComments() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .ownerId(1L)
                .requestId(2L)
                .lastBooking(new BookingItemDto(10L, "Last"))
                .nextBooking(new BookingItemDto(11L, "Next"))
                .comments(List.of(CommentDto.builder()
                        .id(5L)
                        .text("Nice")
                        .authorName("Ivan")
                        .created(LocalDateTime.of(2026, 3, 27, 12, 0))
                        .build()))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);

        assertThat(result).hasJsonPath("$.lastBooking.id");
        assertThat(result).hasJsonPath("$.nextBooking.id");

        assertThat(result).hasJsonPath("$.comments[0].id");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Nice");
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {
        String content = "{"
                + "  \"id\": 1,"
                + "  \"name\": \"Drill\","
                + "  \"description\": \"Good drill\","
                + "  \"available\": true,"
                + "  \"ownerId\": 1"
                + "}";

        ItemDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Drill");
        assertThat(dto.getDescription()).isEqualTo("Good drill");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwnerId()).isEqualTo(1L);
    }
}