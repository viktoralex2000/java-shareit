package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturn201() throws Exception {
        BookingDto request = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto response = BookingDto.builder()
                .id(1L)
                .start(request.getStart())
                .end(request.getEnd())
                .status(BookingStatus.WAITING)
                .booker(BookingUserDto.builder().id(1L).name("Booker").build())
                .item(BookingItemDto.builder().id(1L).name("Item").build())
                .build();

        when(bookingService.create(eq(1L), any(BookingDto.class))).thenReturn(response);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L));
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        BookingDto response = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.APPROVED)
                .build();

        when(bookingService.approve(1L, 1L, true)).thenReturn(response);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        BookingDto response = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getBooking(1L, 1L)).thenReturn(response);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getByUser_shouldReturn200() throws Exception {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getBookingsByUser(1L, "ALL")).thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getByOwner_shouldReturn200() throws Exception {
        BookingDto booking = BookingDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();

        when(bookingService.getBookingsByOwner(1L, "ALL")).thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    // ----------------- Новые тесты (исправленные) -----------------

    @Test
    void create_shouldReturnBadRequestForInvalidDates() throws Exception {
        BookingDto invalid = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(bookingService.create(eq(1L), any(BookingDto.class)))
                .thenThrow(new BadRequestException("Invalid dates"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_shouldReturnNotFoundForNonexistentBooking() throws Exception {
        when(bookingService.getBooking(1L, 999L))
                .thenThrow(new NotFoundException("Booking not found"));

        mvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }
}