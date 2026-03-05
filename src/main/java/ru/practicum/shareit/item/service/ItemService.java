package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemDto getById(Long itemId);

    List<ItemDto> getItemsByOwner(Long ownerId);

    List<ItemDto> search(String text);
}
