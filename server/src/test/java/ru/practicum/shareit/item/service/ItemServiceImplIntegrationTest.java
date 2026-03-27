package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ItemServiceImpl.class)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User owner;
    private User user;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                .name("Owner")
                .email("owner@mail.ru")
                .build());

        user = userRepository.save(User.builder()
                .name("User")
                .email("user@mail.ru")
                .build());

        itemDto = ItemDto.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .build();
    }

    @Test
    void createItem_shouldSaveItemWithOwner() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(savedItem.getId());
        assertEquals("Drill", savedItem.getName());
        assertEquals("Good drill", savedItem.getDescription());
        assertTrue(savedItem.getAvailable());
        assertEquals(owner.getId(), savedItem.getOwnerId());
    }

    @Test
    void createItem_shouldSaveItemWithRequest_whenRequestIdProvided() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requester(user)
                .created(LocalDateTime.now())
                .build());

        itemDto.setRequestId(request.getId());

        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        assertNotNull(savedItem.getId());
        assertEquals(request.getId(), savedItem.getRequestId());
    }

    @Test
    void updateItem_shouldUpdateFields_whenOwner() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Updated")
                .description("Updated desc")
                .available(false)
                .build();

        ItemDto updated = itemService.update(owner.getId(), savedItem.getId(), updateDto);

        assertEquals(savedItem.getId(), updated.getId());
        assertEquals("Updated", updated.getName());
        assertEquals("Updated desc", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    void updateItem_shouldThrowForbidden_whenNotOwner() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder()
                .name("Hack")
                .build();

        assertThrows(ForbiddenException.class,
                () -> itemService.update(user.getId(), savedItem.getId(), updateDto));
    }

    @Test
    void getById_shouldReturnItemWithBookingsAndComments_forOwner() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        Item item = itemRepository.findById(savedItem.getId()).orElseThrow();

        Booking pastBooking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        Booking futureBooking = bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        commentRepository.save(Comment.builder()
                .text("Nice item")
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build());

        ItemDto found = itemService.getById(savedItem.getId(), owner.getId());

        assertNotNull(found.getLastBooking());
        assertEquals(pastBooking.getId(), found.getLastBooking().getId());

        assertNotNull(found.getNextBooking());
        assertEquals(futureBooking.getId(), found.getNextBooking().getId());

        assertNotNull(found.getComments());
        assertEquals(1, found.getComments().size());
        assertEquals("Nice item", found.getComments().get(0).getText());
    }

    @Test
    void getById_shouldReturnItemWithoutBookings_forNotOwner() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        Item item = itemRepository.findById(savedItem.getId()).orElseThrow();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        ItemDto found = itemService.getById(savedItem.getId(), user.getId());

        assertNull(found.getLastBooking());
        assertNull(found.getNextBooking());
    }

    @Test
    void getItemsByOwner_shouldReturnOwnerItemsSorted() {
        ItemDto item1 = itemService.create(owner.getId(), itemDto);

        ItemDto item2 = itemService.create(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Nice hammer")
                .available(true)
                .build());

        List<ItemDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item2.getId(), items.get(1).getId());
    }

    @Test
    void search_shouldReturnAvailableMatchingItems() {
        itemService.create(owner.getId(), itemDto);

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Hammer")
                .description("Tool")
                .available(true)
                .build());

        itemService.create(owner.getId(), ItemDto.builder()
                .name("Hidden drill")
                .description("Not available")
                .available(false)
                .build());

        List<ItemDto> found = itemService.search("drill");

        assertEquals(1, found.size());
        assertEquals("Drill", found.get(0).getName());
    }

    @Test
    void addComment_shouldAddComment_whenUserHadPastApprovedBooking() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        Item item = itemRepository.findById(savedItem.getId()).orElseThrow();

        bookingRepository.save(Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusDays(3))
                .end(LocalDateTime.now().minusDays(2))
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Awesome")
                .build();

        CommentDto savedComment = itemService.addComment(user.getId(), item.getId(), commentDto);

        assertNotNull(savedComment.getId());
        assertEquals("Awesome", savedComment.getText());
        assertEquals(user.getName(), savedComment.getAuthorName());
        assertNotNull(savedComment.getCreated());
    }

    @Test
    void addComment_shouldThrowBadRequest_whenNoPastBooking() {
        ItemDto savedItem = itemService.create(owner.getId(), itemDto);

        CommentDto commentDto = CommentDto.builder()
                .text("Try comment")
                .build();

        assertThrows(BadRequestException.class,
                () -> itemService.addComment(user.getId(), savedItem.getId(), commentDto));
    }
}