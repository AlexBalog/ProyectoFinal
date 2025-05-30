using System;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.ViewModels;

namespace ProyectoFinal.Views
{
    public partial class EventFormWindow : Window
    {
        private EventFormViewModel _viewModel;

        public EventFormWindow(Evento? evento = null)
        {
            InitializeComponent();

            _viewModel = new EventFormViewModel();
            this.DataContext = _viewModel;

            // Configurar el evento para cerrar la ventana
            _viewModel.RequestClose = (result) =>
            {
                this.DialogResult = result;
                this.Close();
            };

            // Configurar el modo según si se pasa un evento o no
            if (evento != null)
            {
                _viewModel.LoadEvent(evento);
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
        /// <returns>True si se creó el evento, False si se canceló</returns>
        public static bool ShowCreateDialog(Window? owner = null)
        {
            var window = new EventFormWindow();
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }

        /// <summary>
        /// Abre la ventana en modo edición
        /// </summary>
        /// <param name="evento">Evento a editar</param>
        /// <param name="owner">Ventana padre</param>
        /// <returns>True si se editó el evento, False si se canceló</returns>
        public static bool ShowEditDialog(Evento evento, Window? owner = null)
        {
            if (evento == null)
                throw new ArgumentNullException(nameof(evento));

            var window = new EventFormWindow(evento);
            if (owner != null)
            {
                window.Owner = owner;
            }

            return window.ShowDialog() ?? false;
        }
        #endregion
    }
}