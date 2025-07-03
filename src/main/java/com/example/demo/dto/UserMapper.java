package com.example.demo.dto;

import com.example.demo.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDto toDto(User user);
    
    List<UserDto> toDtoList(List<User> users);
    
    @Mapping(target = "id", ignore = true)
    User toEntity(UserDto userDto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}
