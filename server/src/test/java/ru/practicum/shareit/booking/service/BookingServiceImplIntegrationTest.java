package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BookingServiceImpl.class)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private User otherUser;

    private Item availableItem;
    private Item unavailableItem;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder().name("Owner").email("owner@mail.ru").build());
        booker = userRepository.save(User.builder().name("Booker").email("booker@mail.ru").build());
        otherUser = userRepository.save(User.builder().name("Other").email("other@mail.ru").build());

        availableItem = itemRepository.save(Item.builder().name("Drill").description("Good drill").available(true).owner(owner).build());
        unavailableItem = itemRepository.save(Item.builder().name("Hammer").description("Old hammer").available(false).owner(owner).build());
    }

    @Test
    void create_shouldCreateBookingWithWaitingStatus() {
        BookingDto dto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto saved = bookingService.create(booker.getId(), dto);

        assertNotNull(saved.getId());
        assertEquals(BookingStatus.WAITING, saved.getStatus());
        assertEquals(booker.getId(), saved.getBooker().getId());
        assertEquals(availableItem.getId(), saved.getItem().getId());
    }

    @Test
    void create_shouldThrowBadRequest_whenItemNotAvailable() {
        BookingDto dto = BookingDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void create_shouldThrowBadRequest_whenEndBeforeStart() {
        BookingDto dto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(BadRequestException.class, () -> bookingService.create(booker.getId(), dto));
    }

    @Test
    void approve_shouldThrowBadRequestOrForbiddenInEdgeCases() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        assertThrows(BadRequestException.class, () -> bookingService.approve(owner.getId(), booking.getId(), true));
        assertThrows(ForbiddenException.class, () -> bookingService.approve(booker.getId(), booking.getId(), false));
    }

    @Test
    void getBooking_shouldThrowNotFoundForNonexistent() {
        assertThrows(NotFoundException.class, () -> bookingService.getBooking(booker.getId(), 999L));
    }

    @Test
    void getBookingsByUser_shouldFilterAllStatesCorrectly() {
        Booking pastBooking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED).build());

        Booking futureBooking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING).build());

        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "PAST").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "FUTURE").size());
        assertEquals(2, bookingService.getBookingsByUser(booker.getId(), "ALL").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "WAITING").size());
    }
}