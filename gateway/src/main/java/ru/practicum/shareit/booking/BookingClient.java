package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingClient {

    private final RestTemplate restTemplate = new RestTemplate(
            new HttpComponentsClientHttpRequestFactory()
    );

    @Value("${shareit-server.url}")
    private String serverUrl;

    private static final String API_PREFIX = "/bookings";

    public ResponseEntity<Object> create(Long userId, BookingDto bookingDto) {
        return sendRequest("", HttpMethod.POST, bookingDto, userId);
    }

    public ResponseEntity<Object> updateStatus(Long userId, Long bookingId, Boolean approved) {
        String path = "/" + bookingId + "?approved=" + approved;
        return sendRequest(path, HttpMethod.PATCH, null, userId);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return sendRequest("/" + bookingId, HttpMethod.GET, null, userId);
    }

    public ResponseEntity<Object> getAllByBooker(Long userId, String state, Integer from, Integer size) {
        String path = "?state=" + state + "&from=" + from + "&size=" + size;
        return sendRequest(path, HttpMethod.GET, null, userId);
    }

    public ResponseEntity<Object> getAllByOwner(Long userId, String state, Integer from, Integer size) {
        String path = "/owner?state=" + state + "&from=" + from + "&size=" + size;
        return sendRequest(path, HttpMethod.GET, null, userId);
    }

    private ResponseEntity<Object> sendRequest(String path,
                                               HttpMethod method,
                                               Object body,
                                               Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (userId != null) {
            headers.set("X-Sharer-User-Id", userId.toString());
        }

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(
                serverUrl + API_PREFIX + path,
                method,
                request,
                Object.class
        );
    }
}