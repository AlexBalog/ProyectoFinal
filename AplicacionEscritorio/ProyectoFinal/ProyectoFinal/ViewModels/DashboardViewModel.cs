using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows;
using ProyectoFinal.Models;
using ProyectoFinal.Services;

namespace ProyectoFinal.ViewModels
{
    public class DashboardViewModel : INotifyPropertyChanged
    {
        private string _currentSectionTitle;
        private object _currentContent;
        private UserData _loggedUser;

        private readonly IUserService _userService;
        private readonly IApiService _apiService;

        public DashboardViewModel()
        {
            _userService = new UserService();
            _apiService = new ApiService();

            // Configurar usuario actual dinámicamente
            InitializeLoggedUser();

            // Título inicial
            CurrentSectionTitle = "Usuarios";
        }

        // Propiedades
        public string CurrentSectionTitle
        {
            get => _currentSectionTitle;
            set
            {
                if (_currentSectionTitle != value)
                {
                    _currentSectionTitle = value;
                    OnPropertyChanged();
                }
            }
        }

        public object CurrentContent
        {
            get => _currentContent;
            set
            {
                if (_currentContent != value)
                {
                    _currentContent = value;
                    OnPropertyChanged();
                }
            }
        }

        // NUEVA PROPIEDAD para usuario logueado
        public UserData LoggedUser
        {
            get => _loggedUser;
            set
            {
                if (_loggedUser != value)
                {
                    _loggedUser = value;
                    OnPropertyChanged();
                    // Notificar cambios en propiedades calculadas
                    OnPropertyChanged(nameof(UserDisplayName));
                    OnPropertyChanged(nameof(UserEmail));
                }
            }
        }

        // Propiedades calculadas para binding directo
        public string UserDisplayName => LoggedUser?.FullName ?? "Administrador";
        public string UserEmail => LoggedUser?.Email ?? "admin@fitsphere.com";

        // Métodos de navegación
        public void LoadUsersSection()
        {
            CurrentSectionTitle = "Gestión de Usuarios";
            CurrentContent = new UserControls.UsersControl();
        }

        public void LoadTrainingsSection()
        {
            CurrentSectionTitle = "Gestión de Entrenamientos";
            CurrentContent = new UserControls.TrainingsControl();
        }

        public void LoadExercisesSection()
        {
            CurrentSectionTitle = "Gestión de Ejercicios";
            CurrentContent = new UserControls.ExercisesControl();
        }

        public void LoadEventsSection()
        {
            CurrentSectionTitle = "Gestión de Eventos";
            CurrentContent = new UserControls.EventsControl();
        }

        public void LoadRequestsSection()
        {
            CurrentSectionTitle = "Peticiones Pendientes";
            CurrentContent = new UserControls.RequestsControl();
        }

        // Método temporal para crear contenido placeholder
        private void CreatePlaceholderContent(string title, string description)
        {
            var panel = new System.Windows.Controls.StackPanel
            {
                HorizontalAlignment = HorizontalAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center
            };

            var titleBlock = new System.Windows.Controls.TextBlock
            {
                Text = title,
                FontSize = 24,
                FontWeight = FontWeights.Bold,
                Foreground = System.Windows.Media.Brushes.White,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 0, 20)
            };

            var descBlock = new System.Windows.Controls.TextBlock
            {
                Text = description,
                FontSize = 14,
                Foreground = System.Windows.Media.Brushes.LightGray,
                HorizontalAlignment = HorizontalAlignment.Center,
                TextWrapping = TextWrapping.Wrap,
                MaxWidth = 400
            };

            panel.Children.Add(titleBlock);
            panel.Children.Add(descBlock);

            CurrentContent = panel;
        }

        // Otros métodos

        public void Logout()
        {
            // Limpiar token y datos de usuario
            _userService.ClearToken();
            _userService.ClearSavedCredentials();
            _userService.ClearUserData(); // NUEVO: Limpiar datos de usuario
            _apiService.ClearAuthToken();
        }

        // MODIFICADO: Inicializar usuario logueado dinámicamente desde datos guardados
        private void InitializeLoggedUser()
        {
            try
            {
                // Intentar obtener datos del usuario desde el servicio
                var userData = _userService.GetUserData();

                if (userData != null)
                {
                    LoggedUser = userData;
                    System.Diagnostics.Debug.WriteLine($"Usuario cargado: {userData.FullName} - {userData.Email}");
                }
                else
                {
                    // Fallback si no hay datos de usuario guardados
                    System.Diagnostics.Debug.WriteLine("No se encontraron datos de usuario guardados, usando valores por defecto");
                    LoggedUser = new UserData
                    {
                        FirstName = "Administrador",
                        LastName = "Sistema",
                        Email = "admin@fitsphere.com",
                        Plan = "Admin"
                    };
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error al inicializar usuario: {ex.Message}");
                // Fallback en caso de error
                LoggedUser = new UserData
                {
                    FirstName = "Administrador",
                    LastName = "Sistema",
                    Email = "admin@fitsphere.com",
                    Plan = "Admin"
                };
            }
        }

        // INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}