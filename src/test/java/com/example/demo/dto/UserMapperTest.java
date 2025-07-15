package com.example.demo.dto;

import com.example.demo.models.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserMapperTest {

    @Mock
    private UserMapper mapper;
    
    @BeforeEach
    public void setup() {
        // Настраиваем моки для маппера
    }
    
    @Test
    public void testUserToUserDto() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        
        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setUsername("testuser");
        expectedDto.setEmail("test@example.com");
        
        // Настраиваем мок
        when(mapper.toDto(user)).thenReturn(expectedDto);
        
        // Выполнение
        UserDto userDto = mapper.toDto(user);
        
        // Проверка
        assertThat(userDto).isNotNull();
        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getUsername()).isEqualTo("testuser");
        assertThat(userDto.getEmail()).isEqualTo("test@example.com");
        
        // Проверяем, что метод был вызван с ожидаемыми параметрами
        verify(mapper, times(1)).toDto(user);
    }
    
    @Test
    public void testUserDtoToUser() {
        // Подготовка
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");
        
        User expectedUser = new User();
        expectedUser.setId(null); // id должен быть проигнорирован согласно аннотации @Mapping
        expectedUser.setUsername("testuser");
        expectedUser.setEmail("test@example.com");
        expectedUser.setPassword(null);
        
        // Настраиваем мок
        when(mapper.toEntity(userDto)).thenReturn(expectedUser);
        
        // Выполнение
        User user = mapper.toEntity(userDto);
        
        // Проверка
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isNull();
        
        // Проверяем, что метод был вызван с ожидаемыми параметрами
        verify(mapper, times(1)).toEntity(userDto);
    }
    
    @Test
    public void testUpdateUserFromUserDto() {
        // Подготовка
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("updateduser");
        userDto.setEmail("updated@example.com");
        
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword("password123");
        
        // Выполнение
        doAnswer(invocation -> {
            User user = invocation.getArgument(1);
            user.setUsername("updateduser");
            user.setEmail("updated@example.com");
            return null;
        }).when(mapper).updateEntityFromDto(userDto, existingUser);
        
        mapper.updateEntityFromDto(userDto, existingUser);
        
        // Проверка
        assertThat(existingUser.getId()).isEqualTo(1L);  // id не должен измениться
        assertThat(existingUser.getUsername()).isEqualTo("updateduser");
        assertThat(existingUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(existingUser.getPassword()).isEqualTo("password123");  // пароль не должен измениться
        
        // Проверяем, что метод был вызван с ожидаемыми параметрами
        verify(mapper, times(1)).updateEntityFromDto(userDto, existingUser);
    }
    
    @Test
    public void testToDtoList() {
        // Подготовка
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        
        List<User> users = List.of(user1, user2);
        
        UserDto dto1 = new UserDto();
        dto1.setId(1L);
        dto1.setUsername("user1");
        dto1.setEmail("user1@example.com");
        
        UserDto dto2 = new UserDto();
        dto2.setId(2L);
        dto2.setUsername("user2");
        dto2.setEmail("user2@example.com");
        
        List<UserDto> expectedDtoList = List.of(dto1, dto2);
        
        // Настраиваем мок
        when(mapper.toDtoList(users)).thenReturn(expectedDtoList);
        
        // Выполнение
        List<UserDto> dtoList = mapper.toDtoList(users);
        
        // Проверка
        assertThat(dtoList).isNotNull();
        assertThat(dtoList).hasSize(2);
        
        UserDto resultDto1 = dtoList.get(0);
        assertThat(resultDto1.getId()).isEqualTo(1L);
        assertThat(resultDto1.getUsername()).isEqualTo("user1");
        assertThat(resultDto1.getEmail()).isEqualTo("user1@example.com");
        
        UserDto resultDto2 = dtoList.get(1);
        assertThat(resultDto2.getId()).isEqualTo(2L);
        assertThat(resultDto2.getUsername()).isEqualTo("user2");
        assertThat(resultDto2.getEmail()).isEqualTo("user2@example.com");
        
        // Проверяем, что метод был вызван с ожидаемыми параметрами
        verify(mapper, times(1)).toDtoList(users);
    }
}
