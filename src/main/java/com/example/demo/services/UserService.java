package com.example.demo.services;

import com.example.demo.dto.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDto> getAllUsers();
    Optional<UserDto> getUserById(Long id);
    Optional<UserDto> getUserByUsername(String username);
    UserDto createUser(UserDto userDto);
    Optional<UserDto> updateUser(Long id, UserDto userDto);
    boolean deleteUser(Long id);
}
