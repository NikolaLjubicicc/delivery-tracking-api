package com.rbt.delivery_tracking.service;

import com.rbt.delivery_tracking.dto.request.UserRequest;
import com.rbt.delivery_tracking.dto.response.UserResponse;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.exception.DuplicateResourceException;
import com.rbt.delivery_tracking.exception.NotFoundException;
import com.rbt.delivery_tracking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_success() {
        when(userRepository.existsByEmail("marko@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = userService.createUser(new UserRequest("Marko Markovic", "marko@example.com", "+381641234567"));

        assertEquals(1L, response.getId().longValue());
        assertEquals("marko@example.com", response.getEmail());
    }

    @Test
    void createUser_duplicateEmail() {
        when(userRepository.existsByEmail("marko@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(new UserRequest("Marko Markovic", "marko@example.com", "+381641234567")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(99L));
    }
}
