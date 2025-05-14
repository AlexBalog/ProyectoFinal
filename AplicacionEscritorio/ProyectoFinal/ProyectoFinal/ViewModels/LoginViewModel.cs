using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Security;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Net.Http;
using System.Text;
using Newtonsoft.Json;
using System.IO;
using System.IO.IsolatedStorage;
using ProyectoFinal.Utilities;

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
        private string _password; // Se usa solo temporalmente durante la validación

        // URL base de la API (ajustar según tu configuración)
        private readonly string ApiBaseUrl = "https://tu-api.com/api";

        // Constructor
        public LoginViewModel()
        {
            // Inicializar propiedades
            RememberMe = false;
            IsLoggingIn = false;
            HasLoginError = false;
            HasEmailError = false;
            HasPasswordError = false;
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

        // Comandos
        private ICommand _loginCommand;
        public ICommand LoginCommand => _loginCommand ?? (_loginCommand = new RelayCommand<PasswordBox>(
            param => LoginAsync(param),
            param => CanLogin(param)));

        private ICommand _forgotPasswordCommand;
        public ICommand ForgotPasswordCommand => _forgotPasswordCommand ?? (_forgotPasswordCommand = new RelayCommand<object>(
            param => ShowForgotPasswordDialog(),
            param => true));

        private ICommand _registerCommand;
        public ICommand RegisterCommand => _registerCommand ?? (_registerCommand = new RelayCommand<object>(
            param => ShowRegisterDialog(),
            param => true));

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

        // Métodos para los comandos
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

                // Intentar obtener acceso a la vista de login para mostrar/ocultar la UI de progreso
                if (Application.Current.MainWindow is Views.LoginView loginView)
                {
                    loginView.HideError();
                    loginView.ShowProgress();
                }

                // Simulación de delay para mostrar el progreso (quitar en implementación real)
                await Task.Delay(1500);

                // Aquí iría la llamada real a tu API para autenticar
                var result = await AuthenticateAsync(Email, _password, RememberMe);

                if (result.IsSuccess)
                {
                    // Autenticación exitosa
                    // Guardar el token JWT si lo has recibido de la API
                    SaveAuthToken(result.Token);

                    // Navegar a la ventana principal
                    Application.Current.Dispatcher.Invoke(() =>
                    {
                        // Crear y mostrar la ventana principal
                        // MainWindow mainWindow = new MainWindow();
                        // mainWindow.Show();

                        // Cerrar la ventana de login
                        Application.Current.MainWindow.Close();
                    });
                }
                else
                {
                    // Error de autenticación
                    LoginErrorMessage = result.ErrorMessage;
                    HasLoginError = true;

                    // Mostrar el error en la UI
                    if (Application.Current.MainWindow is Views.LoginView view)
                    {
                        view.ShowError(result.ErrorMessage);
                    }
                }
            }
            catch (Exception ex)
            {
                // Error inesperado
                LoginErrorMessage = $"Error al iniciar sesión: {ex.Message}";
                HasLoginError = true;

                // Mostrar el error en la UI
                if (Application.Current.MainWindow is Views.LoginView view)
                {
                    view.ShowError($"Error inesperado: {ex.Message}");
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
            }
        }

        private bool CanLogin(PasswordBox passwordBox)
        {
            return !IsLoggingIn && !string.IsNullOrWhiteSpace(Email) &&
                   passwordBox != null && !string.IsNullOrWhiteSpace(passwordBox.Password);
        }

        private void ShowForgotPasswordDialog()
        {
            // Crear y mostrar el diálogo de recuperación de contraseña
            // Ejemplo:
            try
            {
                var forgotPasswordWindow = new Views.ForgotPasswordView();
                forgotPasswordWindow.Owner = Application.Current.MainWindow;
                forgotPasswordWindow.ShowDialog();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"No se pudo abrir la ventana de recuperación de contraseña: {ex.Message}",
                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void ShowRegisterDialog()
        {
            // Crear y mostrar el diálogo de registro
            // Ejemplo:
            try
            {
                // var registerWindow = new Views.RegisterView();
                // registerWindow.Owner = Application.Current.MainWindow;
                // registerWindow.ShowDialog();

                MessageBox.Show("La funcionalidad de registro aún no está implementada.",
                    "Información", MessageBoxButton.OK, MessageBoxImage.Information);
            }
            catch (Exception ex)
            {
                MessageBox.Show($"No se pudo abrir la ventana de registro: {ex.Message}",
                    "Error", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        // Métodos para la autenticación con la API
        private async Task<AuthResult> AuthenticateAsync(string email, string password, bool rememberMe)
        {
            // Implementación de ejemplo - reemplazar con tu llamada API real
            try
            {
                // Simulación - Reemplazar con tu implementación real
                // Para probar, si el email contiene "error", simula un error
                if (email.Contains("error"))
                {
                    return new AuthResult
                    {
                        IsSuccess = false,
                        ErrorMessage = "Credenciales inválidas. Por favor, verifique e intente nuevamente."
                    };
                }

                // En una implementación real, enviarías estos datos a tu API
                var loginData = new
                {
                    Email = email,
                    Password = password,
                    RememberMe = rememberMe
                };

                // Simulación de una respuesta exitosa
                return new AuthResult
                {
                    IsSuccess = true,
                    Token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
                };

                /* Implementación real utilizando HttpClient:
                
                using (var client = new HttpClient())
                {
                    var content = new StringContent(
                        JsonConvert.SerializeObject(loginData),
                        Encoding.UTF8,
                        "application/json");

                    var response = await client.PostAsync($"{ApiBaseUrl}/auth/login", content);
                    
                    if (response.IsSuccessStatusCode)
                    {
                        var responseContent = await response.Content.ReadAsStringAsync();
                        var result = JsonConvert.DeserializeObject<AuthResult>(responseContent);
                        return result;
                    }
                    else
                    {
                        return new AuthResult
                        {
                            IsSuccess = false,
                            ErrorMessage = $"Error de autenticación: {response.StatusCode}"
                        };
                    }
                }
                */
            }
            catch (Exception ex)
            {
                return new AuthResult
                {
                    IsSuccess = false,
                    ErrorMessage = $"Error al autenticar: {ex.Message}"
                };
            }
        }

        private void SaveAuthToken(string token)
        {
            // Método alternativo para guardar el token (en lugar de Properties.Settings.Default)
            // Usamos IsolatedStorage que es más seguro para almacenamiento local
            try
            {
                using (IsolatedStorageFile isoStore = IsolatedStorageFile.GetStore(IsolatedStorageScope.User | IsolatedStorageScope.Assembly, null, null))
                {
                    using (IsolatedStorageFileStream isoStream = new IsolatedStorageFileStream("auth_token.txt", FileMode.Create, isoStore))
                    {
                        using (StreamWriter writer = new StreamWriter(isoStream))
                        {
                            writer.Write(token);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                // Log del error pero continuar
                System.Diagnostics.Debug.WriteLine($"Error al guardar token: {ex.Message}");
            }
        }

        // Implementación de INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }

    // Clases auxiliares
    public class AuthResult
    {
        public bool IsSuccess { get; set; }
        public string Token { get; set; }
        public string ErrorMessage { get; set; }
    }
}