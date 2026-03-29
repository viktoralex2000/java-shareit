package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto dto);

    List<ItemRequestDto> getByOwner(Long userId);

    List<ItemRequestDto> getAll(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}