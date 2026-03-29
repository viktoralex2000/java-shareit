package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@Component
public class BookingMapper {

    public BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(booking.getBooker() != null
                        ? BookingUserDto.builder()
                        .id(booking.getBooker().getId())
                        .name(booking.getBooker().getName())
                        .build()
                        : null)
                .item(booking.getItem() != null
                        ? BookingItemDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build()
                        : null)
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .build();
    }

    public Booking toBooking(BookingDto bookingDto, User booker, Item item) {
        if (bookingDto == null) return null;

        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .booker(booker)
                .item(item)
                .status(bookingDto.getStatus() != null ? bookingDto.getStatus() : BookingStatus.WAITING)
                .build();
    }
}