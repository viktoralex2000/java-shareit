package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        userStorage.getByEmail(user.getEmail()).ifPresent(u -> {
            throw new ConflictException("Пользователь с таким email уже существует");
        });
        User savedUser = userStorage.create(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User updatedUser = UserMapper.toUser(userDto);
        updatedUser.setId(id);
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            user.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) {
            userStorage.getByEmail(updatedUser.getEmail())
                    .filter(u -> !u.getId().equals(updatedUser.getId()))
                    .ifPresent(u -> {
                        throw new ConflictException("Email уже используется");
                    });
            user.setEmail(updatedUser.getEmail());
        }
        User responceUser = userStorage.update(user);
        return UserMapper.toUserDto(responceUser);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userStorage.getById(id).orElseThrow();
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id);
    }
}