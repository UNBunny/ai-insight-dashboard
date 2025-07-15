package com.example.demo.dto;

import com.example.demo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Маппер для преобразования между объектами User и UserDto
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    
    /**
     * Преобразует объект User в UserDto, исключая пароль
     */
    @Mapping(target = "password", ignore = true) // Никогда не возвращаем пароль в DTO
    UserDto toDto(User user);
    
    /**
     * Преобразует список User в список UserDto
     */
    List<UserDto> toDtoList(List<User> users);
    
    /**
     * Преобразует UserDto в новый объект User
     */
    @Mapping(target = "id", ignore = true) // ID назначается базой данных
    User toEntity(UserDto userDto);
    
    /**
     * Обновляет существующий объект User данными из UserDto
     * Обратите внимание, что пароль обрабатывается отдельно в UserServiceImpl
     */
    @Mapping(target = "id", ignore = true) // ID не меняется при обновлении
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}
