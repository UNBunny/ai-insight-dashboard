import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import UserService from '../services/user.service';
import { User, UserCreateDto, UserUpdateDto } from '../interfaces/user.interface';
import { useState } from 'react';

/**
 * Custom hook for working with users through React Query
 * Provides admin panel functionality for user management
 */
export const useUsers = () => {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  
  // Get all users with optional search
  const { 
    data: users = [], 
    isLoading: isLoadingUsers, 
    error: usersError,
    refetch: refetchUsers 
  } = useQuery({
    queryKey: ['users', searchQuery],
    queryFn: () => searchQuery 
      ? UserService.search(searchQuery)
      : UserService.getAll(),
  });
  
  // Получение пользователя по ID
  const useGetUserById = (id: number) => {
    return useQuery({
      queryKey: ['user', id],
      queryFn: () => UserService.getById(id),
      enabled: !!id,
    });
  };
  
  // Создание пользователя
  const createUser = useMutation({
    mutationFn: (user: UserCreateDto) => UserService.create(user),
    onSuccess: () => {
      // Инвалидируем кеш для обновления списка пользователей
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });
  
  // Обновление пользователя
  const updateUser = useMutation({
    mutationFn: ({ id, user }: { id: number; user: UserUpdateDto }) => 
      UserService.update(id, user),
    onSuccess: (_, variables) => {
      // Инвалидируем кеш для обновления пользователя и списка
      queryClient.invalidateQueries({ queryKey: ['user', variables.id] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });
  
  // Удаление пользователя
  const deleteUser = useMutation({
    mutationFn: (id: number) => UserService.delete(id),
    onSuccess: () => {
      // Invalidate cache to update the user list
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  return {
    users,
    isLoadingUsers,
    usersError,
    refetchUsers,
    useGetUserById,
    createUser,
    updateUser,
    deleteUser,
    searchQuery,
    setSearchQuery
  };
};

export default useUsers;
