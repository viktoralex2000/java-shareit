package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.Item;
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
        long id = ++idCounter;
        item.setId(id);
        items.put(id, item);
        return item;
    }

    @Override
    public Item update(Long userId, Long itemId, Item item) {
        items.put(item.getId(), item);
        return items.get(item.getId());
    }

    @Override
    public Optional<Item> getById(Long id) {
        return Optional.ofNullable(items.get(id));
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