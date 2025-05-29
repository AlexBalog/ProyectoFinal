using System;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class ExerciseFormWindow : Window
    {
        private ExerciseFormViewModel _viewModel;

        public ExerciseFormWindow(Ejercicio? exercise = null)
        {
            InitializeComponent();

            _viewModel = new ExerciseFormViewModel();
            this.DataContext = _viewModel;

            // Configurar el evento para cerrar la ventana
            _viewModel.RequestClose = (result) =>
            {
                this.DialogResult = result;
                this.Close();
            };

            // Configurar el modo según si se pasa un ejercicio o no
            if (exercise != null)
            {
                _viewModel.LoadExercise(exercise);
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
        /// <returns>True si se creó el ejercicio, False si se canceló</returns>
        public static bool ShowCreateDialog(Window? owner = null)
        {
            var window = new ExerciseFormWindow();
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }

        /// <summary>
        /// Abre la ventana en modo edición
        /// </summary>
        /// <param name="exercise">Ejercicio a editar</param>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se editó el ejercicio, False si se canceló</returns>
        public static bool ShowEditDialog(Ejercicio exercise, Window? owner = null)
        {
            if (exercise == null)
                throw new ArgumentNullException(nameof(exercise));

            var window = new ExerciseFormWindow(exercise);
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }
        #endregion
    }
}