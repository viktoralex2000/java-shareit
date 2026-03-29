package ru.practicum.shareit.user;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Email(message = "Email должен быть корректен")
    @NotBlank(message = "Email не может быть пустым")
    private String email;
}