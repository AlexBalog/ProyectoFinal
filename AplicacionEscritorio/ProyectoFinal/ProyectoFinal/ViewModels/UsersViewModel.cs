using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class UsersViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Usuario> _usuarios;
        private Usuario _selectedUser;
        private bool _isLoading;
        private string _statusText;

        // Filtros
        private string _filterNombre;
        private string _filterApellido;
        private string _filterEmail;
        private string _filterSexo;
        private string _filterPlan;

        public UsersViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            Usuarios = new ObservableCollection<Usuario>();

            // Inicializar comandos
            InitializeCommands();

            // Cargar datos iniciales
            _ = LoadUsersAsync();
        }

        #region Propiedades
        public ObservableCollection<Usuario> Usuarios
        {
            get => _usuarios;
            set
            {
                if (_usuarios != value)
                {
                    _usuarios = value;
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Usuario SelectedUser
        {
            get => _selectedUser;
            set
            {
                if (_selectedUser != value)
                {
                    _selectedUser = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool IsLoading
        {
            get => _isLoading;
            set
            {
                if (_isLoading != value)
                {
                    _isLoading = value;
                    OnPropertyChanged();
                }
            }
        }

        public string StatusText
        {
            get => _statusText;
            set
            {
                if (_statusText != value)
                {
                    _statusText = value;
                    OnPropertyChanged();
                }
            }
        }

        // Propiedades de filtro
        public string FilterNombre
        {
            get => _filterNombre;
            set
            {
                if (_filterNombre != value)
                {
                    _filterNombre = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterApellido
        {
            get => _filterApellido;
            set
            {
                if (_filterApellido != value)
                {
                    _filterApellido = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterEmail
        {
            get => _filterEmail;
            set
            {
                if (_filterEmail != value)
                {
                    _filterEmail = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterSexo
        {
            get => _filterSexo;
            set
            {
                if (_filterSexo != value)
                {
                    _filterSexo = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterPlan
        {
            get => _filterPlan;
            set
            {
                if (_filterPlan != value)
                {
                    _filterPlan = value;
                    OnPropertyChanged();
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; }
        public ICommand ClearFiltersCommand { get; private set; }
        public ICommand AddUserCommand { get; private set; }
        public ICommand EditUserCommand { get; private set; }
        public ICommand DeleteUserCommand { get; private set; }
        public ICommand RefreshCommand { get; private set; }

        private void InitializeCommands()
        {
            ApplyFiltersCommand = new RelayCommand(async () => await ApplyFiltersAsync());
            ClearFiltersCommand = new RelayCommand(async () => await ClearFiltersAsync());
            AddUserCommand = new RelayCommand(() => AddUser());
            EditUserCommand = new RelayCommand<Usuario>(user => EditUser(user));
            DeleteUserCommand = new RelayCommand<Usuario>(async user => await DeleteUserAsync(user));
            RefreshCommand = new RelayCommand(async () => await LoadUsersAsync());
        }
        #endregion

        #region Métodos
        public async Task LoadUsersAsync()
        {
            try
            {
                IsLoading = true;
                var users = await _dataService.GetAllUsuariosAsync();

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Usuarios.Clear();
                    foreach (var user in users)
                    {
                        Usuarios.Add(user);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar usuarios: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task ApplyFiltersAsync()
        {
            try
            {
                IsLoading = true;

                // Verificar si hay filtros aplicados
                if (string.IsNullOrWhiteSpace(FilterNombre) &&
                    string.IsNullOrWhiteSpace(FilterApellido) &&
                    string.IsNullOrWhiteSpace(FilterEmail) &&
                    string.IsNullOrWhiteSpace(FilterSexo) &&
                    string.IsNullOrWhiteSpace(FilterPlan))
                {
                    // Sin filtros, cargar todos
                    await LoadUsersAsync();
                    return;
                }

                var filter = new UsuarioFilter
                {
                    nombre = FilterNombre,
                    apellido = FilterApellido,
                    email = FilterEmail,
                    sexo = FilterSexo,
                    plan = FilterPlan
                };

                var users = await _dataService.GetUsuariosFiltradosAsync(filter);

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Usuarios.Clear();
                    foreach (var user in users)
                    {
                        Usuarios.Add(user);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al aplicar filtros: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task ClearFiltersAsync()
        {
            FilterNombre = string.Empty;
            FilterApellido = string.Empty;
            FilterEmail = string.Empty;
            FilterSexo = string.Empty;
            FilterPlan = string.Empty;

            await LoadUsersAsync();
        }

        private void AddUser()
        {
            MessageBox.Show("Función para agregar usuario (ventana modal)", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
            // Aquí abriríamos una ventana modal para agregar usuario
        }

        private void EditUser(Usuario user)
        {
            if (user == null) return;

            MessageBox.Show($"Función para editar usuario: {user.NombreCompleto}", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
            // Aquí abriríamos una ventana modal para editar usuario
        }

        private async Task DeleteUserAsync(Usuario user)
        {
            if (user == null) return;

            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas eliminar al usuario '{user.NombreCompleto}'?\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;
                    var success = await _dataService.DeleteUsuarioAsync(user._id);

                    if (success)
                    {
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Usuarios.Remove(user);
                        });

                        UpdateStatusText();
                        MessageBox.Show("Usuario eliminado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                    else
                    {
                        MessageBox.Show("Error al eliminar el usuario.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al eliminar usuario: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        private void UpdateStatusText()
        {
            var count = Usuarios?.Count ?? 0;
            StatusText = $"Mostrando {count} usuario{(count != 1 ? "s" : "")}";
        }

        public async Task RefreshAsync()
        {
            await LoadUsersAsync();
        }
        #endregion

        #region INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        #endregion
    }
}