package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturn201() throws Exception {
        UserDto request = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        UserDto response = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        when(userService.create(any(UserDto.class))).thenReturn(response);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        UserDto response = UserDto.builder()
                .id(1L)
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();

        when(userService.getById(1L)).thenReturn(response);

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.ru"));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        UserDto user1 = UserDto.builder().id(1L).name("Ivan").email("ivan@mail.ru").build();
        UserDto user2 = UserDto.builder().id(2L).name("Petr").email("petr@mail.ru").build();

        when(userService.getAll()).thenReturn(List.of(user1, user2));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UserDto request = UserDto.builder()
                .name("Updated")
                .email("updated@mail.ru")
                .build();

        UserDto response = UserDto.builder()
                .id(1L)
                .name("Updated")
                .email("updated@mail.ru")
                .build();

        when(userService.update(eq(1L), any(UserDto.class))).thenReturn(response);

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.email").value("updated@mail.ru"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(userService).delete(1L);

        mvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());
    }
}