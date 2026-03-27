package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create_shouldReturn201() throws Exception {
        ItemRequestDto request = ItemRequestDto.builder()
                .description("Need item")
                .build();

        ItemRequestDto response = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.now())
                .build();

        when(requestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(response);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need item"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void getByOwner_shouldReturn200() throws Exception {
        ItemRequestDto req = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getByOwner(1L)).thenReturn(List.of(req));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        ItemRequestDto req = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getAll(1L)).thenReturn(List.of(req));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        ItemRequestDto req = ItemRequestDto.builder()
                .id(1L)
                .description("Need item")
                .created(LocalDateTime.now())
                .build();

        when(requestService.getById(1L, 1L)).thenReturn(req);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need item"));
    }
}