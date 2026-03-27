package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemRequestClient requestClient;

    @Test
    void create_shouldReturn400_whenDescriptionBlank() throws Exception {
        String content = """
                {
                  "description": ""
                }
                """;

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}