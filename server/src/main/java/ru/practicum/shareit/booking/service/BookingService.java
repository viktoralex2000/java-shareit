package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto create(Long userId, BookingDto bookingDto);

    BookingDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBooking(Long userId, Long bookingId);

    List<BookingDto> getBookingsByUser(Long userId, String state);

    List<BookingDto> getBookingsByOwner(Long ownerId, String state);
}