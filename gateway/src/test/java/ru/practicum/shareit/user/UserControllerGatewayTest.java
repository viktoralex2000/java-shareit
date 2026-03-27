package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerGatewayTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserClient userClient;

    @Test
    void create_shouldReturn400_whenEmailInvalid() throws Exception {
        String content = """
                {
                  "name": "Ivan",
                  "email": "invalid-email"
                }
                """;

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenNameBlank() throws Exception {
        String content = """
                {
                  "name": "",
                  "email": "ivan@mail.ru"
                }
                """;

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest());
    }
}