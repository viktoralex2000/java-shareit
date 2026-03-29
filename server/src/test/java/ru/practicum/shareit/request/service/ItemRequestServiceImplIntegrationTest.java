package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, ItemRequestMapper.class})
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requester;
    private User otherUser;
    private User thirdUser;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(User.builder()
                .name("Requester")
                .email("requester@mail.ru")
                .build());

        otherUser = userRepository.save(User.builder()
                .name("Other")
                .email("other@mail.ru")
                .build());

        thirdUser = userRepository.save(User.builder()
                .name("Third")
                .email("third@mail.ru")
                .build());
    }

    @Test
    void create_shouldSaveRequest() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        ItemRequestDto saved = requestService.create(requester.getId(), dto);

        assertNotNull(saved.getId());
        assertEquals("Need item", saved.getDescription());
        assertNotNull(saved.getCreated());
        assertEquals(requester.getId(), saved.getRequesterId());
        assertNotNull(saved.getItems());
        assertTrue(saved.getItems().isEmpty());
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotFound() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        assertThrows(NotFoundException.class,
                () -> requestService.create(999L, dto));
    }

    @Test
    void getByOwner_shouldReturnEmptyList_whenUserHasNoRequests() {
        List<ItemRequestDto> requests = requestService.getByOwner(requester.getId());

        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getByOwner_shouldThrowNotFound_whenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> requestService.getByOwner(999L));
    }

    @Test
    void getByOwner_shouldReturnRequestsWithItemsSortedDesc() {
        ItemRequest r1 = requestRepository.save(ItemRequest.builder()
                .description("First")
                .requester(requester)
                .created(LocalDateTime.now().minusDays(2))
                .build());

        ItemRequest r2 = requestRepository.save(ItemRequest.builder()
                .description("Second")
                .requester(requester)
                .created(LocalDateTime.now().minusDays(1))
                .build());

        itemRepository.save(Item.builder()
                .name("Item1")
                .description("Desc")
                .available(true)
                .owner(otherUser)
                .request(r2)
                .build());

        List<ItemRequestDto> requests = requestService.getByOwner(requester.getId());

        assertEquals(2, requests.size());
        assertEquals(r2.getId(), requests.get(0).getId());
        assertEquals(r1.getId(), requests.get(1).getId());

        assertNotNull(requests.get(0).getItems());
        assertEquals(1, requests.get(0).getItems().size());

        assertNotNull(requests.get(1).getItems());
        assertTrue(requests.get(1).getItems().isEmpty());
    }

    @Test
    void getByOwner_shouldReturnRequestsWithMultipleItems() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need something")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Item1")
                .description("Desc1")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build());

        itemRepository.save(Item.builder()
                .name("Item2")
                .description("Desc2")
                .available(true)
                .owner(thirdUser)
                .request(request)
                .build());

        List<ItemRequestDto> requests = requestService.getByOwner(requester.getId());

        assertEquals(1, requests.size());
        assertEquals(request.getId(), requests.get(0).getId());
        assertEquals(2, requests.get(0).getItems().size());
    }

    @Test
    void getAll_shouldThrowNotFound_whenUserNotFound() {
        assertThrows(NotFoundException.class,
                () -> requestService.getAll(999L));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoOtherRequestsExist() {
        requestRepository.save(ItemRequest.builder()
                .description("My request")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> requests = requestService.getAll(requester.getId());

        assertNotNull(requests);
        assertTrue(requests.isEmpty());
    }

    @Test
    void getAll_shouldReturnRequestsOfOthersSortedDesc() {
        ItemRequest r1 = requestRepository.save(ItemRequest.builder()
                .description("Req1")
                .requester(otherUser)
                .created(LocalDateTime.now().minusDays(2))
                .build());

        ItemRequest r2 = requestRepository.save(ItemRequest.builder()
                .description("Req2")
                .requester(otherUser)
                .created(LocalDateTime.now().minusDays(1))
                .build());

        List<ItemRequestDto> requests = requestService.getAll(requester.getId());

        assertEquals(2, requests.size());
        assertEquals(r2.getId(), requests.get(0).getId());
        assertEquals(r1.getId(), requests.get(1).getId());
    }

    @Test
    void getAll_shouldReturnRequestsWithItemsAttached() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requester(otherUser)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .owner(requester)
                .request(request)
                .build());

        List<ItemRequestDto> requests = requestService.getAll(requester.getId());

        assertEquals(1, requests.size());
        assertEquals(request.getId(), requests.get(0).getId());
        assertEquals(1, requests.get(0).getItems().size());
        assertEquals("Drill", requests.get(0).getItems().get(0).getName());
    }

    @Test
    void getAll_shouldNotReturnOwnRequests() {
        requestRepository.save(ItemRequest.builder()
                .description("My request")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        requestRepository.save(ItemRequest.builder()
                .description("Other request")
                .requester(otherUser)
                .created(LocalDateTime.now())
                .build());

        List<ItemRequestDto> requests = requestService.getAll(requester.getId());

        assertEquals(1, requests.size());
        assertEquals("Other request", requests.get(0).getDescription());
        assertEquals(otherUser.getId(), requests.get(0).getRequesterId());
    }

    @Test
    void getById_shouldReturnRequestWithItems() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        itemRepository.save(Item.builder()
                .name("Drill")
                .description("Good drill")
                .available(true)
                .owner(otherUser)
                .request(request)
                .build());

        ItemRequestDto found = requestService.getById(otherUser.getId(), request.getId());

        assertEquals(request.getId(), found.getId());
        assertEquals("Need drill", found.getDescription());
        assertEquals(requester.getId(), found.getRequesterId());
        assertNotNull(found.getCreated());

        assertNotNull(found.getItems());
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().get(0).getName());
    }

    @Test
    void getById_shouldReturnRequestWithEmptyItems_whenNoItemsAttached() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need hammer")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        ItemRequestDto found = requestService.getById(otherUser.getId(), request.getId());

        assertEquals(request.getId(), found.getId());
        assertEquals("Need hammer", found.getDescription());
        assertNotNull(found.getItems());
        assertTrue(found.getItems().isEmpty());
    }

    @Test
    void getById_shouldThrowNotFound_whenUserNotFound() {
        ItemRequest request = requestRepository.save(ItemRequest.builder()
                .description("Need drill")
                .requester(requester)
                .created(LocalDateTime.now())
                .build());

        assertThrows(NotFoundException.class,
                () -> requestService.getById(999L, request.getId()));
    }

    @Test
    void getById_shouldThrowNotFound_whenRequestNotFound() {
        assertThrows(NotFoundException.class,
                () -> requestService.getById(requester.getId(), 999L));
    }
}