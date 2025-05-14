using System;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;

namespace ProyectoFinal.Views
{
    /// <summary>
    /// Lógica de interacción para LoginView.xaml
    /// </summary>
    public partial class LoginView : Window
    {
        private TextBox passwordTextBox;
        private bool isPasswordVisible = false;

        public LoginView()
        {
            InitializeComponent();
        }

        // Método llamado cuando se carga la ventana - inicia animaciones
        private void Window_Loaded(object sender, RoutedEventArgs e)
        {
            // Inicia las animaciones definidas en el XAML
            try
            {
                Storyboard logoAnimation = this.FindResource("LogoAnimation") as Storyboard;
                Storyboard controlsAnimation = this.FindResource("ControlsAnimation") as Storyboard;

                if (logoAnimation != null)
                    logoAnimation.Begin();

                if (controlsAnimation != null)
                    controlsAnimation.Begin();
            }
            catch (Exception ex)
            {
                // Capturar errores en las animaciones para evitar que impidan el inicio
                System.Diagnostics.Debug.WriteLine($"Error al iniciar animaciones: {ex.Message}");
            }
        }

        // Eventos para mover la ventana
        private void Border_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        // Cerrar la ventana
        private void BtnClose_Click(object sender, RoutedEventArgs e)
        {
            this.Close();
        }

        // Minimizar la ventana
        private void BtnMinimize_Click(object sender, RoutedEventArgs e)
        {
            this.WindowState = WindowState.Minimized;
        }

        // Método para mostrar/ocultar la contraseña
        private void ShowPasswordButton_Click(object sender, RoutedEventArgs e)
        {
            // Alternar entre mostrar y ocultar la contraseña
            isPasswordVisible = !isPasswordVisible;

            if (isPasswordVisible)
            {
                // Si el PasswordBox está visible, crear un TextBox y transferir la contraseña
                string currentPassword = passwordBox.Password;

                // Si no se ha creado el TextBox, crearlo
                if (passwordTextBox == null)
                {
                    passwordTextBox = new TextBox();
                    passwordTextBox.FontSize = passwordBox.FontSize;
                    passwordTextBox.Height = passwordBox.Height;
                    passwordTextBox.Width = passwordBox.Width;
                    passwordTextBox.VerticalContentAlignment = passwordBox.VerticalContentAlignment;
                    passwordTextBox.Padding = passwordBox.Padding;
                    passwordTextBox.Foreground = passwordBox.Foreground;
                    passwordTextBox.Background = passwordBox.Background;
                    passwordTextBox.Style = this.FindResource("ModernTextBox") as Style;

                    // Asegurarnos de que cuando el texto cambie en el TextBox, también actualice el PasswordBox
                    passwordTextBox.TextChanged += (s, args) =>
                    {
                        passwordBox.Password = passwordTextBox.Text;
                    };

                    // Agregar el TextBox al panel padre del PasswordBox
                    if (passwordBox.Parent is Grid grid)
                    {
                        grid.Children.Add(passwordTextBox);
                    }
                }

                // Actualizar el texto del TextBox con la contraseña actual
                passwordTextBox.Text = currentPassword;

                // Mostrar el TextBox y ocultar el PasswordBox
                passwordTextBox.Visibility = Visibility.Visible;
                passwordBox.Visibility = Visibility.Hidden;

                // Cambiar el icono del ojo
                EyeIcon.Text = "👁‍🗨"; // Ojo abierto
            }
            else
            {
                // Si el TextBox está visible, transferir el texto al PasswordBox y ocultar el TextBox
                if (passwordTextBox != null && passwordTextBox.Visibility == Visibility.Visible)
                {
                    passwordBox.Password = passwordTextBox.Text;
                    passwordTextBox.Visibility = Visibility.Hidden;
                    passwordBox.Visibility = Visibility.Visible;
                }

                // Cambiar el icono del ojo
                EyeIcon.Text = "👁"; // Ojo cerrado
            }
        }

        // Método para manejar el cambio de contraseña
        private void PasswordBox_PasswordChanged(object sender, RoutedEventArgs e)
        {
            // Actualizar el TextBox si está visible
            if (isPasswordVisible && passwordTextBox != null && passwordTextBox.Visibility == Visibility.Visible)
            {
                // Evitar bucle infinito - solo actualizar si hay diferencia
                if (passwordTextBox.Text != passwordBox.Password)
                {
                    passwordTextBox.Text = passwordBox.Password;
                }
            }

            // Si estás usando MVVM y necesitas pasar la contraseña al ViewModel
            if (DataContext is ViewModels.LoginViewModel viewModel)
            {
                // Usar método seguro para pasar la contraseña al ViewModel sin almacenarla como propiedad
                if (sender is PasswordBox pb)
                {
                    viewModel.SetPassword(pb.Password);
                }
            }
        }

        // Métodos públicos para ser llamados desde el ViewModel (si se necesita)

        // Mostrar mensaje de error
        public void ShowError(string message)
        {
            ErrorText.Text = message;
            ErrorMessage.Visibility = Visibility.Visible;
        }

        // Ocultar mensaje de error
        public void HideError()
        {
            ErrorMessage.Visibility = Visibility.Collapsed;
        }

        // Mostrar barra de progreso
        public void ShowProgress()
        {
            LoginProgress.Visibility = Visibility.Visible;
            LoginButton.IsEnabled = false;
        }

        // Ocultar barra de progreso
        public void HideProgress()
        {
            LoginProgress.Visibility = Visibility.Collapsed;
            LoginButton.IsEnabled = true;
        }
    }
}