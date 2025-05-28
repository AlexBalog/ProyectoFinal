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
                    System.Diagnostics.Debug.WriteLine($"FilterSexo changed to: '{_filterSexo}'");
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
                    System.Diagnostics.Debug.WriteLine($"FilterPlan changed to: '{_filterPlan}'");

                    // Debug adicional para el filtro de Plan
                    if (_filterPlan == "Todos")
                        System.Diagnostics.Debug.WriteLine("Plan filter set to 'Todos' - should show all users");
                    else if (!string.IsNullOrEmpty(_filterPlan))
                        System.Diagnostics.Debug.WriteLine($"Plan filter set to specific value: '{_filterPlan}' - should filter users");
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
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterApellido = string.Empty;
            FilterEmail = string.Empty;
            FilterSexo = "Todos"; // Valor por defecto
            FilterPlan = "Todos"; // Valor por defecto
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

                // Debug: mostrar valores de filtros
                System.Diagnostics.Debug.WriteLine($"Aplicando filtros:");
                System.Diagnostics.Debug.WriteLine($"  Nombre: '{FilterNombre}'");
                System.Diagnostics.Debug.WriteLine($"  Apellido: '{FilterApellido}'");
                System.Diagnostics.Debug.WriteLine($"  Email: '{FilterEmail}'");
                System.Diagnostics.Debug.WriteLine($"  Sexo: '{FilterSexo}'");
                System.Diagnostics.Debug.WriteLine($"  Plan: '{FilterPlan}'");

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

                System.Diagnostics.Debug.WriteLine($"Filtro creado:");
                System.Diagnostics.Debug.WriteLine($"  filter.nombre: '{filter.nombre}'");
                System.Diagnostics.Debug.WriteLine($"  filter.apellido: '{filter.apellido}'");
                System.Diagnostics.Debug.WriteLine($"  filter.email: '{filter.email}'");
                System.Diagnostics.Debug.WriteLine($"  filter.sexo: '{filter.sexo}'");
                System.Diagnostics.Debug.WriteLine($"  filter.plan: '{filter.plan}'");

                // Debug específico para Plan
                if (filter.plan != null)
                {
                    System.Diagnostics.Debug.WriteLine($"PLAN FILTER ACTIVE: Will search for users with plan='{filter.plan}'");
                }
                else
                {
                    System.Diagnostics.Debug.WriteLine($"PLAN FILTER INACTIVE: Will show users with any plan");
                }

                var users = await _dataService.GetUsuariosFiltradosAsync(filter);

                System.Diagnostics.Debug.WriteLine($"Usuarios encontrados: {users.Count}");

                // Debug de los planes de los usuarios encontrados
                if (users.Count > 0)
                {
                    System.Diagnostics.Debug.WriteLine("Planes de usuarios encontrados:");
                    foreach (var user in users.Take(5)) // Mostrar solo los primeros 5 para no spam
                    {
                        System.Diagnostics.Debug.WriteLine($"  {user.NombreCompleto}: Plan='{user.plan}', Sexo='{user.sexo}'");
                    }
                }
                else if (filter.plan != null)
                {
                    System.Diagnostics.Debug.WriteLine($"NO SE ENCONTRARON USUARIOS CON PLAN '{filter.plan}'");
                }

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
                System.Diagnostics.Debug.WriteLine($"Error en ApplyFiltersAsync: {ex}");
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
            FilterSexo = "Todos"; // Volver a "Todos"
            FilterPlan = "Todos"; // Volver a "Todos"

            // Recargar todos los usuarios
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