using System;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class DashboardWindow : Window
    {
        private DashboardViewModel _viewModel;

        public DashboardWindow()
        {
            InitializeComponent();
            _viewModel = DataContext as DashboardViewModel;

            // Cargar la primera sección (Usuarios) por defecto
            LoadUsersSection();
            UpdateActiveButton(btnUsers);
        }

        // Método para mover la ventana
        private void Border_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        // Botones de ventana
        private void BtnMinimize_Click(object sender, RoutedEventArgs e)
        {
            this.WindowState = WindowState.Minimized;
        }

        private void BtnClose_Click(object sender, RoutedEventArgs e)
        {
            Application.Current.Shutdown();
        }

        // Navegación entre secciones
        private void BtnUsers_Click(object sender, RoutedEventArgs e)
        {
            LoadUsersSection();
            UpdateActiveButton(btnUsers);
        }

        private void BtnTrainings_Click(object sender, RoutedEventArgs e)
        {
            LoadTrainingsSection();
            UpdateActiveButton(btnTrainings);
        }

        private void BtnExercises_Click(object sender, RoutedEventArgs e)
        {
            LoadExercisesSection();
            UpdateActiveButton(btnExercises);
        }

        private void BtnEvents_Click(object sender, RoutedEventArgs e)
        {
            LoadEventsSection();
            UpdateActiveButton(btnEvents);
        }

        private void BtnRequests_Click(object sender, RoutedEventArgs e)
        {
            LoadRequestsSection();
            UpdateActiveButton(btnRequests);
        }

        // Métodos para cargar diferentes secciones
        private void LoadUsersSection()
        {
            _viewModel?.LoadUsersSection();
        }

        private void LoadTrainingsSection()
        {
            _viewModel?.LoadTrainingsSection();
        }

        private void LoadExercisesSection()
        {
            _viewModel?.LoadExercisesSection();
        }

        private void LoadEventsSection()
        {
            _viewModel?.LoadEventsSection();
        }

        private void LoadRequestsSection()
        {
            _viewModel?.LoadRequestsSection();
        }


        // Actualizar botón activo
        private void UpdateActiveButton(System.Windows.Controls.Button activeButton)
        {
            // Limpiar estado activo de todos los botones
            btnUsers.Tag = null;
            btnTrainings.Tag = null;
            btnExercises.Tag = null;
            btnEvents.Tag = null;
            btnRequests.Tag = null;

            // Marcar el botón actual como activo
            activeButton.Tag = "Active";
        }

        // Otros botones
        private void BtnLogout_Click(object sender, RoutedEventArgs e)
        {
            var result = MessageBox.Show(
                "¿Estás seguro de que quieres cerrar sesión?",
                "Confirmar",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                // Limpiar tokens y datos de sesión
                _viewModel?.Logout();

                // Mostrar ventana de login
                var loginWindow = new LoginView();
                loginWindow.Show();

                // Cerrar dashboard
                this.Close();
            }
        }
    }
}