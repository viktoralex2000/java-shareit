package ru.practicum.shareit.request;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        if (request == null) {
            return null;
        }

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .requesterId(request.getRequester() != null ? request.getRequester().getId() : null)
                .created(request.getCreated())
                .items(mapItems(items))
                .build();
    }

    public ItemRequest toItemRequest(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .created(dto.getCreated())
                .build();
    }

    private List<ItemDto> mapItems(List<Item> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> ItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }
}