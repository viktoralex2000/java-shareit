package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create_shouldReturn400_whenStartNull() throws Exception {
        String content = "{"
                + "  \"itemId\": 1,"
                + "  \"end\": \"2026-03-28T10:00:00\""
                + "}";

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenItemIdNull() throws Exception {
        String content = "{"
                + "  \"start\": \"2026-03-27T10:00:00\","
                + "  \"end\": \"2026-03-28T10:00:00\""
                + "}";

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}