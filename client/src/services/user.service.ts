import api from '../utils/api';
import { User, UserCreateDto, UserUpdateDto } from '../interfaces/user.interface';

const BASE_URL = '/api/v1/users';

/**
 * Service for managing users through the admin panel
 * Connects to the backend REST API endpoints
 */
export const UserService = {
  /**
   * Получить список всех пользователей
   */
  async getAll(): Promise<User[]> {
    return api.get<User[]>(BASE_URL);
  },

  /**
   * Получить пользователя по ID
   */
  async getById(id: number): Promise<User> {
    return api.get<User>(`${BASE_URL}/${id}`);
  },

  /**
   * Создать нового пользователя
   */
  async create(user: UserCreateDto): Promise<User> {
    return api.post<User>(BASE_URL, user);
  },

  /**
   * Обновить существующего пользователя
   */
  async update(id: number, user: UserUpdateDto): Promise<User> {
    return api.request<User>(`${BASE_URL}/${id}`, {
      method: 'PUT',
      body: JSON.stringify(user),
      headers: {
        'Content-Type': 'application/json'
      }
    });
  },

  /**
   * Удалить пользователя
   */
  async delete(id: number): Promise<void> {
    return api.request<void>(`${BASE_URL}/${id}`, {
      method: 'DELETE'
    });
  },

  /**
   * Search users by criteria
   */
  async search(query: string): Promise<User[]> {
    return api.get<User[]>(`${BASE_URL}/search?query=${encodeURIComponent(query)}`);
  }
};

export default UserService;
