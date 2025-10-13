package com.funkard.service;

import com.funkard.model.User;
import com.funkard.repository.UserRepository;
import com.funkard.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<UserDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public UserDTO create(User user) {
        return toDTO(repo.save(user));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setEmail(u.getEmail());
        dto.setUsername(u.getUsername());
        dto.setName(u.getName());
        dto.setAvatarUrl(u.getAvatarUrl());
        dto.setRole(u.getRole());
        return dto;
    }
}