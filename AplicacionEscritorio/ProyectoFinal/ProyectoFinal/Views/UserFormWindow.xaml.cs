using System;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class UserFormWindow : Window
    {
        private UserFormViewModel _viewModel;

        public UserFormWindow(Usuario? user = null)
        {
            InitializeComponent();

            _viewModel = new UserFormViewModel();
            this.DataContext = _viewModel;

            // Configurar el evento para cerrar la ventana
            _viewModel.RequestClose = (result) =>
            {
                this.DialogResult = result;
                this.Close();
            };

            // Configurar el modo según si se pasa un usuario o no
            if (user != null)
            {
                _viewModel.LoadUser(user);
            }
            else
            {
                _viewModel.SetCreateMode();
            }
        }

        #region Eventos de ventana
        private void Header_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        private void BtnClose_Click(object sender, RoutedEventArgs e)
        {
            this.DialogResult = false;
            this.Close();
        }
        #endregion

        #region Eventos de PasswordBox
        // Como WPF no permite binding directo en PasswordBox por seguridad,
        // manejamos los eventos manualmente

        private void TxtPassword_PasswordChanged(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null && sender is System.Windows.Controls.PasswordBox passwordBox)
            {
                _viewModel.Contrasena = passwordBox.Password;
            }
        }

        private void TxtConfirmPassword_PasswordChanged(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null && sender is System.Windows.Controls.PasswordBox passwordBox)
            {
                _viewModel.ConfirmarContrasena = passwordBox.Password;
            }
        }

        private void TxtNewPassword_PasswordChanged(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null && sender is System.Windows.Controls.PasswordBox passwordBox)
            {
                _viewModel.Contrasena = passwordBox.Password;
            }
        }

        private void TxtConfirmNewPassword_PasswordChanged(object sender, RoutedEventArgs e)
        {
            if (_viewModel != null && sender is System.Windows.Controls.PasswordBox passwordBox)
            {
                _viewModel.ConfirmarContrasena = passwordBox.Password;
            }
        }
        #endregion

        #region Métodos públicos estáticos para facilitar el uso
        /// <summary>
        /// Abre la ventana en modo creación
        /// </summary>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se creó el usuario, False si se canceló</returns>
        public static bool ShowCreateDialog(Window? owner = null)
        {
            var window = new UserFormWindow();
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }

        /// <summary>
        /// Abre la ventana en modo edición
        /// </summary>
        /// <param name="user">Usuario a editar</param>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se editó el usuario, False si se canceló</returns>
        public static bool ShowEditDialog(Usuario user, Window? owner = null)
        {
            if (user == null)
                throw new ArgumentNullException(nameof(user));

            var window = new UserFormWindow(user);
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }
        #endregion
    }
}