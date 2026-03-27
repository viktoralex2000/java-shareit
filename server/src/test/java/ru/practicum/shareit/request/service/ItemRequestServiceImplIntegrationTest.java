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
@Import(ItemRequestServiceImpl.class)
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
    }

    @Test
    void create_shouldSaveRequest() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Need item").build();
        ItemRequestDto saved = requestService.create(requester.getId(), dto);

        assertNotNull(saved.getId());
        assertEquals("Need item", saved.getDescription());
        assertNotNull(saved.getCreated());
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
        assertEquals(1, requests.get(0).getItems().size());
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
        assertEquals(1, found.getItems().size());
        assertEquals("Drill", found.getItems().get(0).getName());
    }

    @Test
    void getById_shouldThrowNotFound_whenRequestNotFound() {
        assertThrows(NotFoundException.class,
                () -> requestService.getById(requester.getId(), 999L));
    }
}