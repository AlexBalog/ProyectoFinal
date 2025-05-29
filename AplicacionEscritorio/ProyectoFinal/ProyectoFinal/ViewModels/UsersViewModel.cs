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
using ProyectoFinal.Views;

namespace ProyectoFinal.ViewModels
{
    public class UsersViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Usuario> _usuarios;
        private Usuario? _selectedUser;
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
            _usuarios = new ObservableCollection<Usuario>();

            // Inicializar propiedades con valores por defecto
            _statusText = "Cargando...";
            _filterNombre = string.Empty;
            _filterApellido = string.Empty;
            _filterEmail = string.Empty;
            _filterSexo = "Todos";
            _filterPlan = "Todos";

            // Inicializar filtros con valores por defecto
            InitializeFilters();

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
                    _usuarios = value ?? throw new ArgumentNullException(nameof(value));
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Usuario? SelectedUser
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
                    _statusText = value ?? string.Empty;
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
                    _filterNombre = value ?? string.Empty;
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
                    _filterApellido = value ?? string.Empty;
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
                    _filterEmail = value ?? string.Empty;
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
                    _filterSexo = value ?? "Todos";
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
                    _filterPlan = value ?? "Todos";
                    OnPropertyChanged();
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; } = null!;
        public ICommand ClearFiltersCommand { get; private set; } = null!;
        public ICommand AddUserCommand { get; private set; } = null!;
        public ICommand EditUserCommand { get; private set; } = null!;
        public ICommand DeleteUserCommand { get; private set; } = null!;
        public ICommand RefreshCommand { get; private set; } = null!;

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
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterApellido = string.Empty;
            FilterEmail = string.Empty;
            FilterSexo = "Todos";
            FilterPlan = "Todos";
        }

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

                // Verificar si hay filtros aplicados (excluyendo "Todos" que representa sin filtro)
                bool hasFilters = !string.IsNullOrWhiteSpace(FilterNombre) ||
                                  !string.IsNullOrWhiteSpace(FilterApellido) ||
                                  !string.IsNullOrWhiteSpace(FilterEmail) ||
                                  (!string.IsNullOrEmpty(FilterSexo) && FilterSexo != "Todos") ||
                                  (!string.IsNullOrEmpty(FilterPlan) && FilterPlan != "Todos");

                if (!hasFilters)
                {
                    // Sin filtros, cargar todos
                    await LoadUsersAsync();
                    return;
                }

                var filter = new UsuarioFilter
                {
                    nombre = string.IsNullOrWhiteSpace(FilterNombre) ? null : FilterNombre,
                    apellido = string.IsNullOrWhiteSpace(FilterApellido) ? null : FilterApellido,
                    email = string.IsNullOrWhiteSpace(FilterEmail) ? null : FilterEmail,
                    sexo = (string.IsNullOrEmpty(FilterSexo) || FilterSexo == "Todos") ? null : FilterSexo,
                    plan = (string.IsNullOrEmpty(FilterPlan) || FilterPlan == "Todos") ? null : FilterPlan
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
            // Limpiar todos los filtros
            FilterNombre = string.Empty;
            FilterApellido = string.Empty;
            FilterEmail = string.Empty;
            FilterSexo = "Todos";
            FilterPlan = "Todos";

            // Recargar todos los usuarios
            await LoadUsersAsync();
        }

        private void AddUser()
        {
            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de creación
                bool result = UserFormWindow.ShowCreateDialog(mainWindow);

                // Si se creó el usuario exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadUsersAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de creación: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void EditUser(Usuario? user)
        {
            if (user == null) return;

            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de edición
                bool result = UserFormWindow.ShowEditDialog(user, mainWindow);

                // Si se editó el usuario exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadUsersAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de edición: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task DeleteUserAsync(Usuario? user)
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
        public event PropertyChangedEventHandler? PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string? propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        #endregion
    }
}