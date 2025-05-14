using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class ForgotPasswordViewModel : INotifyPropertyChanged
    {
        private string _email;
        private bool _isProcessing;
        private string _errorMessage;
        private bool _hasError;
        private string _successMessage;
        private bool _hasSuccess;

        public string Email
        {
            get => _email;
            set
            {
                if (_email != value)
                {
                    _email = value;
                    OnPropertyChanged();
                    ValidateEmail();
                }
            }
        }

        public bool IsProcessing
        {
            get => _isProcessing;
            set
            {
                if (_isProcessing != value)
                {
                    _isProcessing = value;
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(IsNotProcessing));
                }
            }
        }

        public bool IsNotProcessing => !IsProcessing;

        public string ErrorMessage
        {
            get => _errorMessage;
            set
            {
                if (_errorMessage != value)
                {
                    _errorMessage = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasError
        {
            get => _hasError;
            set
            {
                if (_hasError != value)
                {
                    _hasError = value;
                    OnPropertyChanged();
                }
            }
        }

        public string SuccessMessage
        {
            get => _successMessage;
            set
            {
                if (_successMessage != value)
                {
                    _successMessage = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasSuccess
        {
            get => _hasSuccess;
            set
            {
                if (_hasSuccess != value)
                {
                    _hasSuccess = value;
                    OnPropertyChanged();
                }
            }
        }

        // Comandos
        private ICommand _resetPasswordCommand;
        public ICommand ResetPasswordCommand => _resetPasswordCommand ?? (_resetPasswordCommand = new RelayCommand<object>(
            param => ResetPassword(),
            param => CanResetPassword()));

        // Constructor
        public ForgotPasswordViewModel()
        {
            IsProcessing = false;
            HasError = false;
            HasSuccess = false;
        }

        // Métodos
        private void ValidateEmail()
        {
            HasError = false;
            ErrorMessage = string.Empty;

            if (string.IsNullOrWhiteSpace(Email))
            {
                ErrorMessage = "El correo electrónico es obligatorio";
                HasError = true;
                return;
            }

            if (!Email.Contains("@") || !Email.Contains("."))
            {
                ErrorMessage = "Formato de correo electrónico inválido";
                HasError = true;
                return;
            }
        }

        private bool CanResetPassword()
        {
            return !string.IsNullOrWhiteSpace(Email) && !HasError && !IsProcessing;
        }

        private async void ResetPassword()
        {
            ValidateEmail();
            if (HasError)
                return;

            try
            {
                IsProcessing = true;
                HasSuccess = false;
                HasError = false;

                // Simulación de proceso - reemplazar con tu llamada a API real
                await Task.Delay(2000);

                // Simulación de respuesta exitosa
                SuccessMessage = $"Se ha enviado un correo de recuperación a {Email}. Por favor revise su bandeja de entrada.";
                HasSuccess = true;

                // En un escenario real, aquí iría el código para llamar a tu API
                // y procesar la recuperación de contraseña.
            }
            catch (Exception ex)
            {
                ErrorMessage = $"Error al procesar la solicitud: {ex.Message}";
                HasError = true;
            }
            finally
            {
                IsProcessing = false;
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