import React, { useState, useEffect } from 'react';
import { useUsers } from '../../hooks/useUsers';
import { User, UserCreateDto, UserUpdateDto } from '../../interfaces/user.interface';
import { Button } from '../../components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../../components/ui/card';
import { 
  Table, 
  TableBody, 
  TableCell, 
  TableHead, 
  TableHeader, 
  TableRow 
} from '../../components/ui/table';
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogFooter, 
  DialogHeader, 
  DialogTitle 
} from '../../components/ui/dialog';
import { Input } from '../../components/ui/input';
import { Label } from '../../components/ui/label';
import { AlertCircle, PenSquare, Trash2, Search } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '../../components/ui/alert';

const UsersPage: React.FC = () => {
  const { 
    users, 
    isLoadingUsers, 
    usersError, 
    createUser, 
    updateUser, 
    deleteUser,
    searchQuery,
    setSearchQuery
  } = useUsers();
  
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [isUpdateDialogOpen, setIsUpdateDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<UserCreateDto | UserUpdateDto>({
    username: '',
    email: '',
    password: '',
  });

  // Обработчики формы
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  // Создание пользователя
  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await createUser.mutateAsync(formData as UserCreateDto);
      setIsCreateDialogOpen(false);
      setFormData({ username: '', email: '', password: '' });
    } catch (error) {
      console.error('Error creating user:', error);
    }
  };

  // Обновление пользователя
  const handleUpdateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedUser?.id) return;
    
    try {
      await updateUser.mutateAsync({ 
        id: selectedUser.id, 
        user: {
          email: formData.email,
          firstName: (formData as any).firstName,
          lastName: (formData as any).lastName,
        } 
      });
      setIsUpdateDialogOpen(false);
    } catch (error) {
      console.error('Error updating user:', error);
    }
  };

  // Удаление пользователя
  const handleDeleteUser = async () => {
    if (!selectedUser?.id) return;
    
    try {
      await deleteUser.mutateAsync(selectedUser.id);
      setIsDeleteDialogOpen(false);
    } catch (error) {
      console.error('Error deleting user:', error);
    }
  };

  // Открытие диалога обновления
  const openUpdateDialog = (user: User) => {
    setSelectedUser(user);
    setFormData({
      email: user.email,
      firstName: user.firstName || '',
      lastName: user.lastName || '',
    });
    setIsUpdateDialogOpen(true);
  };

  // Открытие диалога удаления
  const openDeleteDialog = (user: User) => {
    setSelectedUser(user);
    setIsDeleteDialogOpen(true);
  };

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">User Management</h1>
          <p className="text-muted-foreground">Manage system users and their permissions</p>
        </div>
        <Button 
          onClick={() => {
            setFormData({ username: '', email: '', password: '', firstName: '', lastName: '' });
            setIsCreateDialogOpen(true);
          }}
          className="bg-blue-600 hover:bg-blue-700"
        >
          Add New User
        </Button>
      </div>

      <Card className="border border-slate-200 dark:border-slate-700 shadow-sm">
        <CardHeader className="bg-slate-50 dark:bg-slate-800 border-b border-slate-200 dark:border-slate-700">
          <div className="flex justify-between items-center">
            <CardTitle className="text-slate-900 dark:text-white text-xl">Users</CardTitle>
            
            <div className="relative">
              <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
                <Search className="h-4 w-4 text-gray-500" />
              </div>
              <Input
                type="search"
                placeholder="Search users..."
                className="pl-10 w-[250px]"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {usersError && (
            <Alert variant="destructive" className="mb-4">
              <AlertCircle className="h-4 w-4" />
              <AlertTitle>Ошибка</AlertTitle>
              <AlertDescription>
                Не удалось загрузить пользователей. Проверьте подключение к серверу.
              </AlertDescription>
            </Alert>
          )}

          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>ID</TableHead>
                <TableHead>Логин</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Имя</TableHead>
                <TableHead>Фамилия</TableHead>
                <TableHead>Дата создания</TableHead>
                <TableHead>Действия</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoadingUsers ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center">Загрузка...</TableCell>
                </TableRow>
              ) : users.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center">Нет данных</TableCell>
                </TableRow>
              ) : (
                users.map((user) => (
                  <TableRow key={user.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                    <TableCell className="font-medium">{user.id}</TableCell>
                    <TableCell>{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{user.firstName || '-'}</TableCell>
                    <TableCell>{user.lastName || '-'}</TableCell>
                    <TableCell>
                      {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : '-'}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="h-8 w-8 p-0 text-blue-600 hover:text-blue-700 hover:bg-blue-50 dark:hover:bg-slate-700"
                          onClick={() => openUpdateDialog(user)}
                        >
                          <PenSquare className="h-4 w-4" />
                        </Button>
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="h-8 w-8 p-0 text-red-600 hover:text-red-700 hover:bg-red-50 dark:hover:bg-slate-700" 
                          onClick={() => openDeleteDialog(user)}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* User Creation Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="sm:max-w-[500px] bg-white dark:bg-slate-800">
          <DialogHeader>
            <DialogTitle>Создать нового пользователя</DialogTitle>
            <DialogDescription>
              Заполните данные для создания нового пользователя.
            </DialogDescription>
          </DialogHeader>
          
          <form onSubmit={handleCreateUser} className="space-y-4">
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="username" className="text-right">Логин</Label>
                <Input
                  id="username"
                  name="username"
                  value={(formData as UserCreateDto).username || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="email" className="text-right">Email</Label>
                <Input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="password" className="text-right">Пароль</Label>
                <Input
                  id="password"
                  name="password"
                  type="password"
                  value={(formData as UserCreateDto).password || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="firstName" className="text-right">Имя</Label>
                <Input
                  id="firstName"
                  name="firstName"
                  value={(formData as any).firstName || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="lastName" className="text-right">Фамилия</Label>
                <Input
                  id="lastName"
                  name="lastName"
                  value={(formData as any).lastName || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                />
              </div>
            </div>
            
            <DialogFooter>
              <Button
                type="button" 
                variant="outline" 
                onClick={() => setIsCreateDialogOpen(false)}
              >
                Отмена
              </Button>
              <Button 
                type="submit"
                disabled={createUser.isPending}
              >
                {createUser.isPending ? 'Создание...' : 'Создать'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Диалог обновления пользователя */}
      <Dialog open={isUpdateDialogOpen} onOpenChange={setIsUpdateDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Обновить пользователя</DialogTitle>
            <DialogDescription>
              Измените данные пользователя {selectedUser?.username}.
            </DialogDescription>
          </DialogHeader>
          
          <form onSubmit={handleUpdateUser} className="space-y-4">
            <div className="grid gap-4 py-4">
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="update-email" className="text-right">Email</Label>
                <Input
                  id="update-email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className="col-span-3"
                  required
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="update-firstName" className="text-right">Имя</Label>
                <Input
                  id="update-firstName"
                  name="firstName"
                  value={(formData as any).firstName || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                />
              </div>
              <div className="grid grid-cols-4 items-center gap-2">
                <Label htmlFor="update-lastName" className="text-right">Фамилия</Label>
                <Input
                  id="update-lastName"
                  name="lastName"
                  value={(formData as any).lastName || ''}
                  onChange={handleInputChange}
                  className="col-span-3"
                />
              </div>
            </div>
            
            <DialogFooter>
              <Button
                type="button" 
                variant="outline" 
                onClick={() => setIsUpdateDialogOpen(false)}
              >
                Отмена
              </Button>
              <Button 
                type="submit"
                disabled={updateUser.isPending}
              >
                {updateUser.isPending ? 'Обновление...' : 'Обновить'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Диалог подтверждения удаления */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Подтверждение удаления</DialogTitle>
            <DialogDescription>
              Вы действительно хотите удалить пользователя {selectedUser?.username}?
              Это действие нельзя отменить.
            </DialogDescription>
          </DialogHeader>
          
          <DialogFooter>
            <Button
              type="button" 
              variant="outline" 
              onClick={() => setIsDeleteDialogOpen(false)}
            >
              Отмена
            </Button>
            <Button 
              type="button"
              variant="destructive"
              disabled={deleteUser.isPending}
              onClick={handleDeleteUser}
            >
              {deleteUser.isPending ? 'Удаление...' : 'Удалить'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default UsersPage;
