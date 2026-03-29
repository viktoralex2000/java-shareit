package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public UserDto create(UserDto userDto) {
        User user = mapper.toUser(userDto);

        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new ConflictException("Пользователь с таким email уже существует");
        });

        User savedUser = userRepository.save(user);
        return mapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {

            userRepository.findByEmail(userDto.getEmail())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new ConflictException("Email уже используется");
                    });

            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return mapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return mapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(mapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        userRepository.deleteById(id);
    }
}