/*package ru.practicum.shareit.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    private LocalDateTime end;

    @NotNull(message = "Id вещи обязателен")
    private Long itemId;

    private BookingStatus status;

    private BookingUserDto booker;  // вложенный объект для booker
    private BookingItemDto item;     // вложенный объект для item
}

// вспомогательный DTO для booker
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BookingUserDto {
    private Long id;
    private String name;
}

// вспомогательный DTO для item
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BookingItemDto {
    private Long id;
    private String name;
}*/

package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "Дата начала бронирования обязательна")
    @Future(message = "Дата начала должна быть в будущем")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    private LocalDateTime end;

    @NotNull(message = "Id вещи обязателен")
    private Long itemId;

    private BookingStatus status;

    private BookingUserDto booker;  // вложенный объект для booker
    private BookingItemDto item;     // вложенный объект для item
}