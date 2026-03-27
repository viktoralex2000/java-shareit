package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_shouldReturn400_whenNameBlank() throws Exception {
        String content = """
                {
                  "name": "",
                  "description": "Some description",
                  "available": true
                }
                """;

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenDescriptionBlank() throws Exception {
        String content = """
                {
                  "name": "Drill",
                  "description": "",
                  "available": true
                }
                """;

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenAvailableNull() throws Exception {
        String content = """
                {
                  "name": "Drill",
                  "description": "Some description"
                }
                """;

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}