using System;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class TrainingFormWindow : Window
    {
        private TrainingFormViewModel _viewModel;

        public TrainingFormWindow(Entrenamiento? training = null)
        {
            InitializeComponent();

            _viewModel = new TrainingFormViewModel();
            this.DataContext = _viewModel;

            // Configurar el evento para cerrar la ventana
            _viewModel.RequestClose = (result) =>
            {
                this.DialogResult = result;
                this.Close();
            };

            // Configurar el modo según si se pasa un entrenamiento o no
            if (training != null)
            {
                _viewModel.LoadTraining(training);
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

        #region Métodos públicos estáticos para facilitar el uso
        /// <summary>
        /// Abre la ventana en modo creación
        /// </summary>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se creó el entrenamiento, False si se canceló</returns>
        public static bool ShowCreateDialog(Window? owner = null)
        {
            var window = new TrainingFormWindow();
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }

        /// <summary>
        /// Abre la ventana en modo edición
        /// </summary>
        /// <param name="training">Entrenamiento a editar</param>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se editó el entrenamiento, False si se canceló</returns>
        public static bool ShowEditDialog(Entrenamiento training, Window? owner = null)
        {
            if (training == null)
                throw new ArgumentNullException(nameof(training));

            var window = new TrainingFormWindow(training);
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }
        #endregion
    }
}