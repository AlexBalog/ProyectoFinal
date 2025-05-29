using System;
using System.Collections.Generic;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class ExerciseSelectionWindow : Window
    {
        private ExerciseSelectionViewModel _viewModel;

        public ExerciseSelectionWindow(List<string> alreadySelectedIds = null)
        {
            InitializeComponent();

            _viewModel = new ExerciseSelectionViewModel(alreadySelectedIds);
            this.DataContext = _viewModel;

            // Configurar el evento para cerrar la ventana
            _viewModel.RequestClose = (selectedExercise) =>
            {
                this.SelectedExercise = selectedExercise;
                this.DialogResult = selectedExercise != null;
                this.Close();
            };
        }

        public Ejercicio SelectedExercise { get; private set; }

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
        /// Abre la ventana de selección de ejercicio
        /// </summary>
        /// <param name="alreadySelectedIds">IDs de ejercicios ya seleccionados (para no mostrarlos como disponibles)</param>
        /// <param name="owner">Ventana padre</param>
        /// <returns>El ejercicio seleccionado o null si se canceló</returns>
        public static Ejercicio ShowSelectionDialog(List<string> alreadySelectedIds = null, Window owner = null)
        {
            var window = new ExerciseSelectionWindow(alreadySelectedIds);
            if (owner != null)
            {
                window.Owner = owner;
            }

            bool? result = window.ShowDialog();
            return result == true ? window.SelectedExercise : null;
        }
        #endregion

        private void ExerciseCard_Click(object sender, MouseButtonEventArgs e)
        {
            if (sender is Border border && border.Tag is Ejercicio ejercicio)
            {
                // CORRECCIÓN: Usar el ViewModel para establecer la selección
                // Esto asegura que se dispare el PropertyChanged correctamente
                _viewModel.SelectedExercise = ejercicio;

                // Actualizar visualmente la selección
                UpdateVisualSelection(border);
            }
        }

        private void UpdateVisualSelection(Border selectedBorder)
        {
            // Buscar el ItemsControl que contiene todas las tarjetas
            var itemsControl = FindVisualParent<ItemsControl>(selectedBorder);
            if (itemsControl != null)
            {
                // Resetear todos los bordes
                foreach (var item in itemsControl.Items)
                {
                    var container = itemsControl.ItemContainerGenerator.ContainerFromItem(item);
                    if (container != null)
                    {
                        var border = FindVisualChild<Border>(container);
                        if (border != null)
                        {
                            // Restablecer estilo normal
                            border.Background = (System.Windows.Media.Brush)FindResource("SurfaceBrush");
                            border.BorderBrush = (System.Windows.Media.Brush)FindResource("AccentBrush");
                            border.BorderThickness = new System.Windows.Thickness(1);
                        }
                    }
                }

                // Resaltar el seleccionado
                selectedBorder.Background = System.Windows.Media.Brushes.DarkMagenta;
                selectedBorder.BorderBrush = (System.Windows.Media.Brush)FindResource("PrimaryBrush");
                selectedBorder.BorderThickness = new System.Windows.Thickness(2);
            }
        }

        private T FindVisualParent<T>(System.Windows.DependencyObject obj) where T : System.Windows.DependencyObject
        {
            while (obj != null)
            {
                if (obj is T parent)
                    return parent;
                obj = System.Windows.Media.VisualTreeHelper.GetParent(obj);
            }
            return null;
        }

        private T FindVisualChild<T>(System.Windows.DependencyObject obj) where T : System.Windows.DependencyObject
        {
            for (int i = 0; i < System.Windows.Media.VisualTreeHelper.GetChildrenCount(obj); i++)
            {
                var child = System.Windows.Media.VisualTreeHelper.GetChild(obj, i);
                if (child is T directChild)
                    return directChild;

                var foundChild = FindVisualChild<T>(child);
                if (foundChild != null)
                    return foundChild;
            }
            return null;
        }
    }
}