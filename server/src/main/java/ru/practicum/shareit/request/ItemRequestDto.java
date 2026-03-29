package ru.practicum.shareit.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.practicum.shareit.item.ItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    private Long id;

    @NotBlank(message = "Описание запроса не может быть пустым")
    private String description;

    private Long requesterId;

    private LocalDateTime created;

    @Builder.Default
    private List<ItemDto> items = new ArrayList<>();
}