package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("Ivan")
                .email("ivan@mail.ru")
                .build();
    }

    @Test
    void createUser_shouldSaveUser() {
        UserDto savedUser = userService.create(userDto);

        assertNotNull(savedUser.getId());
        assertEquals(userDto.getName(), savedUser.getName());
        assertEquals(userDto.getEmail(), savedUser.getEmail());

        assertTrue(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void createUser_shouldThrowConflict_whenEmailExists() {
        userService.create(userDto);

        UserDto secondUser = UserDto.builder()
                .name("Petr")
                .email("ivan@mail.ru")
                .build();

        assertThrows(ConflictException.class, () -> userService.create(secondUser));
    }

    @Test
    void updateUser_shouldUpdateNameAndEmail() {
        UserDto savedUser = userService.create(userDto);

        UserDto updateDto = UserDto.builder()
                .name("UpdatedName")
                .email("updated@mail.ru")
                .build();

        UserDto updatedUser = userService.update(savedUser.getId(), updateDto);

        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("UpdatedName", updatedUser.getName());
        assertEquals("updated@mail.ru", updatedUser.getEmail());
    }

    @Test
    void getById_shouldReturnUser() {
        UserDto savedUser = userService.create(userDto);

        UserDto foundUser = userService.getById(savedUser.getId());

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getName(), foundUser.getName());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void delete_shouldDeleteUser() {
        UserDto savedUser = userService.create(userDto);

        userService.delete(savedUser.getId());

        assertFalse(userRepository.existsById(savedUser.getId()));
        assertThrows(NotFoundException.class, () -> userService.getById(savedUser.getId()));
    }
}