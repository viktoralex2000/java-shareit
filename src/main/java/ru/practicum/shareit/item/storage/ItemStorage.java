package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item item);

    Optional<Item> getById(Long id);

    List<Item> getByOwner(Long ownerId);

    List<Item> search(String text);

    boolean existsById(Long id);
}