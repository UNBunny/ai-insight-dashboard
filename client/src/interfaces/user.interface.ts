export interface User {
  id?: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserCreateDto {
  username: string;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
}

export interface UserUpdateDto {
  email?: string;
  firstName?: string;
  lastName?: string;
}
