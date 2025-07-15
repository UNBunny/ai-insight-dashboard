package com.example.demo.services.impl;

import com.example.demo.dto.UserDto;
import com.example.demo.dto.UserMapper;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Реализация сервиса для работы с пользователями
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Override
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.debug("Создание нового пользователя: {}", userDto.getUsername());
        User user = userMapper.toEntity(userDto);
        
        // Хешируем пароль перед сохранением
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        User savedUser = userRepository.save(user);
        log.debug("Пользователь создан с ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public Optional<UserDto> updateUser(Long id, UserDto userDto) {
        log.debug("Обновление пользователя с ID: {}", id);
        return userRepository.findById(id)
                .map(existingUser -> {
                    // Сохраняем текущий пароль, если в DTO не передан новый
                    String currentPassword = existingUser.getPassword();
                    
                    // Обновляем поля из DTO
                    userMapper.updateEntityFromDto(userDto, existingUser);
                    
                    // Если пароль был обновлен (не равен хешированному), хешируем его
                    if (userDto.getPassword() != null && !userDto.getPassword().isEmpty() && 
                        !passwordEncoder.matches(userDto.getPassword(), currentPassword)) {
                        existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
                    } else {
                        // Иначе восстанавливаем текущий хешированный пароль
                        existingUser.setPassword(currentPassword);
                    }
                    
                    User updatedUser = userRepository.save(existingUser);
                    log.debug("Пользователь обновлен: {}", updatedUser.getId());
                    return userMapper.toDto(updatedUser);
                });
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        log.debug("Удаление пользователя с ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            log.debug("Пользователь удален: {}", id);
            return true;
        }
        log.debug("Пользователь с ID {} не найден для удаления", id);
        return false;
    }
    
    /**
     * Проверяет, совпадает ли пароль с хешированным значением в базе
     * 
     * @param rawPassword сырой (нехешированный) пароль
     * @param username имя пользователя
     * @return true если пароли совпадают
     */
    public boolean validatePassword(String rawPassword, String username) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }
}
