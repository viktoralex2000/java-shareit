package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemStorage {

    Item create(Long userId, Item item);

    Item update(Long userId, Long itemId, Item item);

    Item getById(Long itemId, Long userId);

    List<Item> getByOwner(Long ownerId);

    List<Item> search(String text);

    boolean existsById(Long id);
}