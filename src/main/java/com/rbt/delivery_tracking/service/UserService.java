package com.rbt.delivery_tracking.service;

import com.rbt.delivery_tracking.dto.request.UserRequest;
import com.rbt.delivery_tracking.dto.response.PageResponse;
import com.rbt.delivery_tracking.dto.response.UserResponse;
import com.rbt.delivery_tracking.entity.User;
import com.rbt.delivery_tracking.exception.DuplicateResourceException;
import com.rbt.delivery_tracking.exception.NotFoundException;
import com.rbt.delivery_tracking.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }
        User user = new User(request.getFullName(), request.getEmail(), request.getPhone());
        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " not found"));
        return toResponse(user);
    }

    public PageResponse<UserResponse> getAll(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> content = new ArrayList<>();
        for (User user : page.getContent()) {
            content.add(toResponse(user));
        }
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getCreatedAt());
    }
}
