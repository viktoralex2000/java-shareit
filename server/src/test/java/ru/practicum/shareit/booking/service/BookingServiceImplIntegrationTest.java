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
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        booker = userRepository.save(User.builder()
                .name("Booker")
                .email("booker@mail.ru")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.ru")
                .build());

        availableItem = itemRepository.save(Item.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .owner(owner)
                .build());

        unavailableItem = itemRepository.save(Item.builder()
                .name("Hammer")
                .description("Old hammer")
                .available(false)
                .owner(owner)
                .build());
    }

    @Test
    void create_shouldCreateBookingWithWaitingStatus() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingDto saved = bookingService.create(booker.getId(), bookingDto);

        assertNotNull(saved.getId());
        assertEquals(BookingStatus.WAITING, saved.getStatus());
        assertNotNull(saved.getBooker());
        assertEquals(booker.getId(), saved.getBooker().getId());
        assertNotNull(saved.getItem());
        assertEquals(availableItem.getId(), saved.getItem().getId());
    }

    @Test
    void create_shouldThrowBadRequest_whenItemNotAvailable() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(unavailableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(BadRequestException.class,
                () -> bookingService.create(booker.getId(), bookingDto));
    }

    @Test
    void approve_shouldApproveBooking_whenOwnerAndWaiting() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
    }

    @Test
    void approve_shouldRejectBooking_whenOwnerAndApprovedFalse() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
    }

    @Test
    void approve_shouldThrowForbidden_whenNotOwner() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        assertThrows(ForbiddenException.class,
                () -> bookingService.approve(booker.getId(), booking.getId(), true));
    }

    @Test
    void approve_shouldThrowBadRequest_whenAlreadyApproved() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        assertThrows(BadRequestException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), true));
    }

    @Test
    void getBooking_shouldReturnBooking_whenBooker() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto found = bookingService.getBooking(booker.getId(), booking.getId());

        assertEquals(booking.getId(), found.getId());
    }

    @Test
    void getBooking_shouldReturnBooking_whenOwner() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto found = bookingService.getBooking(owner.getId(), booking.getId());

        assertEquals(booking.getId(), found.getId());
    }

    @Test
    void getBooking_shouldThrowForbidden_whenNotOwnerAndNotBooker() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        assertThrows(ForbiddenException.class,
                () -> bookingService.getBooking(otherUser.getId(), booking.getId()));
    }

    @Test
    void getBookingsByUser_shouldFilterByState() {
        bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING)
                .build());

        List<BookingDto> past = bookingService.getBookingsByUser(booker.getId(), "PAST");
        assertEquals(1, past.size());

        List<BookingDto> waiting = bookingService.getBookingsByUser(booker.getId(), "WAITING");
        assertEquals(1, waiting.size());

        List<BookingDto> all = bookingService.getBookingsByUser(booker.getId(), "ALL");
        assertEquals(2, all.size());
    }

    @Test
    void getBookingsByOwner_shouldFilterByState() {
        bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.REJECTED)
                .build());

        List<BookingDto> rejected = bookingService.getBookingsByOwner(owner.getId(), "REJECTED");
        assertEquals(1, rejected.size());

        List<BookingDto> future = bookingService.getBookingsByOwner(owner.getId(), "FUTURE");
        assertEquals(1, future.size());

        List<BookingDto> all = bookingService.getBookingsByOwner(owner.getId(), "ALL");
        assertEquals(2, all.size());
    }
}