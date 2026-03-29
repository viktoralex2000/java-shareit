package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate(
            new HttpComponentsClientHttpRequestFactory()
    );

    @Value("${shareit-server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(UserDto userDto) {
        return sendRequest("/users", HttpMethod.POST, userDto);
    }

    public ResponseEntity<Object> update(Long userId, UserDto userDto) {
        Map<String, Object> updates = new HashMap<>();
        if (userDto.getName() != null) {
            updates.put("name", userDto.getName());
        }
        if (userDto.getEmail() != null) {
            updates.put("email", userDto.getEmail());
        }

        return sendRequest("/users/{id}", HttpMethod.PATCH, updates, userId);
    }

    public ResponseEntity<Object> getById(Long userId) {
        return sendRequest("/users/{id}", HttpMethod.GET, null, userId);
    }

    public ResponseEntity<Object> getAll() {
        return sendRequest("/users", HttpMethod.GET, null);
    }

    public ResponseEntity<Object> delete(Long userId) {
        return sendRequest("/users/{id}", HttpMethod.DELETE, null, userId);
    }

    private ResponseEntity<Object> sendRequest(String path,
                                               HttpMethod method,
                                               Object body,
                                               Object... uriVariables) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(
                serverUrl + path,
                method,
                request,
                Object.class,
                uriVariables
        );
    }
}