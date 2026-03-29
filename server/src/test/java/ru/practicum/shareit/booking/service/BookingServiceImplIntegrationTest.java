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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookingServiceImpl.class, BookingMapper.class})
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
    void create_shouldThrowNotFound_whenUserDoesNotExist() {
        BookingDto dto = BookingDto.builder()
                .itemId(availableItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.create(999L, dto));
    }

    @Test
    void create_shouldThrowNotFound_whenItemDoesNotExist() {
        BookingDto dto = BookingDto.builder()
                .itemId(999L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(NotFoundException.class, () -> bookingService.create(booker.getId(), dto));
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
    void approve_shouldApproveBooking_whenOwnerApproves() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approved.getStatus());
        assertEquals(booking.getId(), approved.getId());
    }

    @Test
    void approve_shouldRejectBooking_whenOwnerRejects() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
        assertEquals(booking.getId(), rejected.getId());
    }

    @Test
    void approve_shouldThrowNotFound_whenBookingNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.approve(owner.getId(), 999L, true));
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
    void getBooking_shouldReturnBooking_whenBookerRequests() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto dto = bookingService.getBooking(booker.getId(), booking.getId());

        assertEquals(booking.getId(), dto.getId());
        assertEquals(booker.getId(), dto.getBooker().getId());
        assertEquals(availableItem.getId(), dto.getItem().getId());
    }

    @Test
    void getBooking_shouldReturnBooking_whenOwnerRequests() {
        Booking booking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build());

        BookingDto dto = bookingService.getBooking(owner.getId(), booking.getId());

        assertEquals(booking.getId(), dto.getId());
    }

    @Test
    void getBooking_shouldThrowForbidden_whenOtherUserRequests() {
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
    void getBooking_shouldThrowNotFoundForNonexistent() {
        assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(booker.getId(), 999L));
    }

    @Test
    void getBookingsByUser_shouldThrowNotFound_whenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByUser(999L, "ALL"));
    }

    @Test
    void getBookingsByOwner_shouldThrowNotFound_whenOwnerNotFound() {
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByOwner(999L, "ALL"));
    }

    @Test
    void getBookingsByUser_shouldReturnEmptyList_whenNoBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByUser(booker.getId(), "ALL");

        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }

    @Test
    void getBookingsByOwner_shouldReturnEmptyList_whenNoBookings() {
        List<BookingDto> bookings = bookingService.getBookingsByOwner(owner.getId(), "ALL");

        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
    }

    @Test
    void getBookingsByUser_shouldFilterAllStatesCorrectly() {
        Booking pastBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        Booking futureBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING)
                .build());

        Booking rejectedBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(5))
                .status(BookingStatus.REJECTED)
                .build());

        Booking currentBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build());

        assertEquals(4, bookingService.getBookingsByUser(booker.getId(), "ALL").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "PAST").size());
        assertEquals(pastBooking.getId(), bookingService.getBookingsByUser(booker.getId(), "PAST").get(0).getId());

        assertEquals(2, bookingService.getBookingsByUser(booker.getId(), "FUTURE").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "WAITING").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "REJECTED").size());
        assertEquals(1, bookingService.getBookingsByUser(booker.getId(), "CURRENT").size());
        assertEquals(currentBooking.getId(), bookingService.getBookingsByUser(booker.getId(), "CURRENT").get(0).getId());
    }

    @Test
    void getBookingsByOwner_shouldFilterAllStatesCorrectly() {
        Booking pastBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        Booking futureBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3))
                .status(BookingStatus.WAITING)
                .build());

        Booking rejectedBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(4))
                .end(LocalDateTime.now().plusDays(5))
                .status(BookingStatus.REJECTED)
                .build());

        Booking currentBooking = bookingRepository.save(Booking.builder()
                .item(availableItem)
                .booker(booker)
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .status(BookingStatus.APPROVED)
                .build());

        assertEquals(4, bookingService.getBookingsByOwner(owner.getId(), "ALL").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "PAST").size());
        assertEquals(2, bookingService.getBookingsByOwner(owner.getId(), "FUTURE").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "WAITING").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "REJECTED").size());
        assertEquals(1, bookingService.getBookingsByOwner(owner.getId(), "CURRENT").size());

        assertEquals(currentBooking.getId(),
                bookingService.getBookingsByOwner(owner.getId(), "CURRENT").get(0).getId());
    }

}