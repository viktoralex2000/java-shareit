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
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

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
    void approve_shouldApproveAndReject() {
        Booking booking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build());

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);
        assertEquals(BookingStatus.APPROVED, approved.getStatus());

        Booking booking2 = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(3)).end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING).build());

        BookingDto rejected = bookingService.approve(owner.getId(), booking2.getId(), false);
        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void approve_shouldThrowForbiddenOrBadRequest() {
        Booking booking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build());

        assertThrows(ForbiddenException.class, () -> bookingService.approve(booker.getId(), booking.getId(), true));

        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);

        assertThrows(BadRequestException.class, () -> bookingService.approve(owner.getId(), booking.getId(), true));
    }

    @Test
    void getBooking_shouldReturnOrThrowForbidden() {
        Booking booking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(1)).end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING).build());

        BookingDto byBooker = bookingService.getBooking(booker.getId(), booking.getId());
        assertEquals(booking.getId(), byBooker.getId());

        BookingDto byOwner = bookingService.getBooking(owner.getId(), booking.getId());
        assertEquals(booking.getId(), byOwner.getId());

        assertThrows(ForbiddenException.class, () -> bookingService.getBooking(otherUser.getId(), booking.getId()));
    }

    @Test
    void getBookingsByUserAndOwner_shouldFilterCorrectly() {
        Booking pastBooking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().minusDays(3)).end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED).build());

        Booking futureBooking = bookingRepository.save(Booking.builder().item(availableItem).booker(booker)
                .start(LocalDateTime.now().plusDays(2)).end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING).build());

        // By User
        List<BookingDto> past = bookingService.getBookingsByUser(booker.getId(), "PAST");
        assertEquals(1, past.size());
        assertEquals(pastBooking.getId(), past.get(0).getId());

        List<BookingDto> waiting = bookingService.getBookingsByUser(booker.getId(), "WAITING");
        assertEquals(1, waiting.size());
        assertEquals(futureBooking.getId(), waiting.get(0).getId());

        List<BookingDto> all = bookingService.getBookingsByUser(booker.getId(), "ALL");
        assertEquals(2, all.size());

        // By Owner
        List<BookingDto> rejected = bookingService.getBookingsByOwner(owner.getId(), "REJECTED");
        assertEquals(0, rejected.size());

        List<BookingDto> future = bookingService.getBookingsByOwner(owner.getId(), "FUTURE");
        assertEquals(1, future.size());
        assertEquals(futureBooking.getId(), future.get(0).getId());

        List<BookingDto> allOwner = bookingService.getBookingsByOwner(owner.getId(), "ALL");
        assertEquals(2, allOwner.size());
    }
}