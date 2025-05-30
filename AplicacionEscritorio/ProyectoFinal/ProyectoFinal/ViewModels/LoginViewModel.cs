using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;
using ProyectoFinal.Views;
using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace ProyectoFinal.ViewModels
{
    public class LoginViewModel : INotifyPropertyChanged
    {
        // Propiedades privadas
        private string _email;
        private bool _rememberMe;
        private bool _isLoggingIn;
        private string _loginErrorMessage;
        private bool _hasLoginError;
        private string _emailError;
        private bool _hasEmailError;
        private string _passwordError;
        private bool _hasPasswordError;
        private string _password;

        // Servicios
        private readonly IApiService _apiService;
        private readonly IUserService _userService;

        // Constructor
        public LoginViewModel()
        {
            // Inicializar servicios
            _apiService = new ApiService();
            _userService = new UserService();

            // Inicializar propiedades
            RememberMe = false;
            IsLoggingIn = false;
            HasLoginError = false;
            HasEmailError = false;
            HasPasswordError = false;

            // Cargar credenciales guardadas si existen
            LoadSavedCredentials();
        }

        // Propiedades públicas con notificación de cambios
        public string Email
        {
            get => _email;
            set
            {
                if (_email != value)
                {
                    _email = value;
                    ValidateEmail();
                    OnPropertyChanged();
                }
            }
        }

        public bool RememberMe
        {
            get => _rememberMe;
            set
            {
                if (_rememberMe != value)
                {
                    _rememberMe = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool IsLoggingIn
        {
            get => _isLoggingIn;
            set
            {
                if (_isLoggingIn != value)
                {
                    _isLoggingIn = value;
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(IsNotLoggingIn));
                }
            }
        }

        public bool IsNotLoggingIn => !IsLoggingIn;

        public string LoginErrorMessage
        {
            get => _loginErrorMessage;
            set
            {
                if (_loginErrorMessage != value)
                {
                    _loginErrorMessage = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasLoginError
        {
            get => _hasLoginError;
            set
            {
                if (_hasLoginError != value)
                {
                    _hasLoginError = value;
                    OnPropertyChanged();
                }
            }
        }

        public string EmailError
        {
            get => _emailError;
            set
            {
                if (_emailError != value)
                {
                    _emailError = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasEmailError
        {
            get => _hasEmailError;
            set
            {
                if (_hasEmailError != value)
                {
                    _hasEmailError = value;
                    OnPropertyChanged();
                }
            }
        }

        public string PasswordError
        {
            get => _passwordError;
            set
            {
                if (_passwordError != value)
                {
                    _passwordError = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasPasswordError
        {
            get => _hasPasswordError;
            set
            {
                if (_hasPasswordError != value)
                {
                    _hasPasswordError = value;
                    OnPropertyChanged();
                }
            }
        }

        // Método para establecer la contraseña de manera segura
        public void SetPassword(string password)
        {
            _password = password;
            ValidatePassword();
        }

        // Comando de login
        private ICommand _loginCommand;
        public ICommand LoginCommand => _loginCommand ?? (_loginCommand = new RelayCommand<PasswordBox>(
            param => LoginAsync(param),
            param => CanLogin(param)));

        // Métodos de validación
        private void ValidateEmail()
        {
            if (string.IsNullOrWhiteSpace(Email))
            {
                EmailError = "El correo electrónico es obligatorio";
                HasEmailError = true;
                return;
            }

            // Validación simple de formato de email
            if (!Email.Contains("@") || !Email.Contains("."))
            {
                EmailError = "Formato de correo electrónico inválido";
                HasEmailError = true;
                return;
            }

            HasEmailError = false;
            EmailError = string.Empty;
        }

        private void ValidatePassword()
        {
            if (string.IsNullOrWhiteSpace(_password))
            {
                PasswordError = "La contraseña es obligatoria";
                HasPasswordError = true;
                return;
            }

            if (_password.Length < 6)
            {
                PasswordError = "La contraseña debe tener al menos 6 caracteres";
                HasPasswordError = true;
                return;
            }

            HasPasswordError = false;
            PasswordError = string.Empty;
        }

        private bool Validate()
        {
            ValidateEmail();
            ValidatePassword();

            return !HasEmailError && !HasPasswordError;
        }

        // Método para el comando de login
        private async void LoginAsync(PasswordBox passwordBox)
        {
            // Obtener la contraseña del PasswordBox
            if (passwordBox != null)
            {
                _password = passwordBox.Password;
            }

            // Validar datos
            if (!Validate())
            {
                return;
            }

            try
            {
                // Iniciar el proceso de login
                IsLoggingIn = true;
                HasLoginError = false;

                // Mostrar progreso en la UI
                if (Application.Current.MainWindow is Views.LoginView loginView)
                {
                    loginView.HideError();
                    loginView.ShowProgress();
                }

                // Crear la solicitud de login
                var loginRequest = new LoginRequest
                {
                    Email = Email,
                    Password = _password
                };

                // Llamar a la API
                var result = await _apiService.LoginAsync(loginRequest);

                if (result.IsSuccess)
                {
                    // Autenticación exitosa
                    // Guardar el token JWT
                    if (!string.IsNullOrEmpty(result.Token))
                    {
                        _userService.SaveToken(result.Token);
                    }

                    // Guardar los datos del usuario
                    if (result.User != null)
                    {
                        _userService.SaveUserData(result.User);
                    }

                    // Navegar a la ventana principal
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        var dashboardWindow = new DashboardWindow();
                        dashboardWindow.Show();

                        // Por ahora, mostrar un mensaje de éxito
                        MessageBox.Show($"¡Bienvenido {result.User?.FirstName ?? Email}! Login exitoso.", "Éxito", MessageBoxButton.OK, MessageBoxImage.Information);

                        // Cerrar la ventana de login
                        Application.Current.MainWindow.Close();
                    });
                }
                else
                {
                    // Error de autenticación
                    LoginErrorMessage = result.ErrorMessage ?? "Error de autenticación. Verifique sus credenciales.";
                    HasLoginError = true;

                    // Mostrar el error en la UI con el tipo específico
                    if (Application.Current.MainWindow is Views.LoginView view)
                    {
                        view.ShowError(LoginErrorMessage, result.ErrorType ?? "general");
                    }
                }
            }
            catch (Exception ex)
            {
                // Error inesperado
                LoginErrorMessage = $"Error al conectar con el servidor: {ex.Message}";
                HasLoginError = true;

                // Mostrar el error en la UI
                if (Application.Current.MainWindow is Views.LoginView view)
                {
                    view.ShowError(LoginErrorMessage, "connection");
                }
            }
            finally
            {
                // Finalizar el proceso de login
                IsLoggingIn = false;

                // Ocultar el progreso en la UI
                if (Application.Current.MainWindow is Views.LoginView view)
                {
                    view.HideProgress();
                }

                // Limpiar la contraseña por seguridad
                _password = null;
                if (passwordBox != null)
                {
                    passwordBox.Clear();
                }
            }
        }

        private bool CanLogin(PasswordBox passwordBox)
        {
            return !IsLoggingIn &&
                   !string.IsNullOrWhiteSpace(Email) &&
                   passwordBox != null &&
                   !string.IsNullOrWhiteSpace(passwordBox.Password);
        }

        // Método para cargar credenciales guardadas
        private void LoadSavedCredentials()
        {
            try
            {
                var savedCredentials = _userService.GetSavedCredentials();
                if (savedCredentials != null && !string.IsNullOrEmpty(savedCredentials.Email))
                {
                    Email = savedCredentials.Email;
                    RememberMe = true;
                }
            }
            catch (Exception ex)
            {
                // Error al cargar credenciales - continuar normalmente
                System.Diagnostics.Debug.WriteLine($"Error al cargar credenciales guardadas: {ex.Message}");
            }
        }

        // Implementación de INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}