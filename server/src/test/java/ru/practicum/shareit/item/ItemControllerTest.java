package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturn201() throws Exception {
        ItemDto request = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.create(eq(1L), any(ItemDto.class))).thenReturn(response);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.description").value("Good drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        ItemDto request = ItemDto.builder()
                .name("Updated")
                .build();

        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Updated")
                .description("Good drill")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.update(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(response);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        ItemDto response = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.getById(1L, 2L)).thenReturn(response);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getItemsByOwner_shouldReturn200() throws Exception {
        ItemDto item1 = ItemDto.builder().id(1L).name("Item1").description("Desc1").available(true).build();
        ItemDto item2 = ItemDto.builder().id(2L).name("Item2").description("Desc2").available(true).build();

        when(itemService.getItemsByOwner(1L)).thenReturn(List.of(item1, item2));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void search_shouldReturn200() throws Exception {
        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();

        when(itemService.search("drill")).thenReturn(List.of(item));

        mvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void addComment_shouldReturn201() throws Exception {
        CommentDto request = CommentDto.builder()
                .text("Nice")
                .build();

        CommentDto response = CommentDto.builder()
                .id(1L)
                .text("Nice")
                .authorName("Ivan")
                .build();

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(response);

        mvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Nice"))
                .andExpect(jsonPath("$.authorName").value("Ivan"));
    }
}