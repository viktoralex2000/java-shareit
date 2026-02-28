package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.storage.UserStorage;

import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ItemInMemoryStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 0;

    private final UserStorage userStorage;

    @Override
    public Item create(Long userId, Item item) {
        User owner = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        item.setOwner(owner);
        long id = ++idCounter;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        Item existingItem = items.get(itemId);
        if (existingItem == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Только владелец может редактировать вещь");
        }
        if (item.getName() != null && !item.getName().isBlank()) {
            existingItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            existingItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }
        items.put(existingItem.getId(), existingItem);
        return existingItem;
    }

    @Override
    public Item getById(Long itemId, Long userId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        return item;
    }

    @Override
    public List<Item> getByOwner(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();
        return items.values().stream()
                .filter(i -> i.getAvailable() &&
                        (i.getName().toLowerCase().contains(lowerText) ||
                                i.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }
}