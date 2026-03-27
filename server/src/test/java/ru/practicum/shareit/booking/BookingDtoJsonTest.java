package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto dto = BookingDto.builder()
                .id(1L)
                .itemId(2L)
                .start(LocalDateTime.of(2026, 3, 27, 10, 0))
                .end(LocalDateTime.of(2026, 3, 28, 10, 0))
                .status(BookingStatus.WAITING)
                .booker(new BookingUserDto(3L, "Ivan"))
                .item(new BookingItemDto(2L, "Drill"))
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");

        assertThat(result).hasJsonPath("$.booker.id");
        assertThat(result).hasJsonPath("$.item.id");
    }

    @Test
    void shouldDeserializeBookingDto() throws Exception {
        String content = "{"
                + "  \"id\": 1,"
                + "  \"itemId\": 2,"
                + "  \"start\": \"2026-03-27T10:00:00\","
                + "  \"end\": \"2026-03-28T10:00:00\","
                + "  \"status\": \"WAITING\""
                + "}";

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 3, 27, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 3, 28, 10, 0));
    }
}