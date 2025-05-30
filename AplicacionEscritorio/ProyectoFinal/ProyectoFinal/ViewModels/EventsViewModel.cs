using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class EventsViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Evento> _eventos;
        private Evento _selectedEvent;
        private bool _isLoading;
        private string _statusText;

        // Filtros
        private string _filterNombre;
        private string _filterTipo;

        public EventsViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            Eventos = new ObservableCollection<Evento>();

            // Inicializar filtros con valores por defecto
            InitializeFilters();

            // Inicializar comandos
            InitializeCommands();

            // Cargar datos iniciales
            _ = LoadEventsAsync();
        }

        #region Propiedades
        public ObservableCollection<Evento> Eventos
        {
            get => _eventos;
            set
            {
                if (_eventos != value)
                {
                    _eventos = value;
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Evento SelectedEvent
        {
            get => _selectedEvent;
            set
            {
                if (_selectedEvent != value)
                {
                    _selectedEvent = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool IsLoading
        {
            get => _isLoading;
            set
            {
                if (_isLoading != value)
                {
                    _isLoading = value;
                    OnPropertyChanged();
                }
            }
        }

        public string StatusText
        {
            get => _statusText;
            set
            {
                if (_statusText != value)
                {
                    _statusText = value;
                    OnPropertyChanged();
                }
            }
        }

        // Propiedades de filtro
        public string FilterNombre
        {
            get => _filterNombre;
            set
            {
                if (_filterNombre != value)
                {
                    _filterNombre = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterTipo
        {
            get => _filterTipo;
            set
            {
                if (_filterTipo != value)
                {
                    _filterTipo = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterTipo changed to: '{_filterTipo}'");
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; }
        public ICommand ClearFiltersCommand { get; private set; }
        public ICommand AddEventCommand { get; private set; }
        public ICommand EditEventCommand { get; private set; }
        public ICommand DeleteEventCommand { get; private set; }
        public ICommand RefreshCommand { get; private set; }

        private void InitializeCommands()
        {
            ApplyFiltersCommand = new RelayCommand(async () => await ApplyFiltersAsync());
            ClearFiltersCommand = new RelayCommand(async () => await ClearFiltersAsync());
            AddEventCommand = new RelayCommand(() => AddEvent());
            EditEventCommand = new RelayCommand<Evento>(eventItem => EditEvent(eventItem));
            DeleteEventCommand = new RelayCommand<Evento>(async eventItem => await DeleteEventAsync(eventItem));
            RefreshCommand = new RelayCommand(async () => await LoadEventsAsync());
        }
        #endregion

        #region Métodos
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterTipo = "Todos"; // Valor por defecto
        }

        public async Task LoadEventsAsync()
        {
            try
            {
                IsLoading = true;
                var events = await _dataService.GetAllEventosAsync();

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Eventos.Clear();
                    foreach (var eventItem in events)
                    {
                        Eventos.Add(eventItem);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar eventos: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task ApplyFiltersAsync()
        {
            try
            {
                IsLoading = true;

                // Debug: mostrar valores de filtros
                System.Diagnostics.Debug.WriteLine($"Aplicando filtros de eventos:");
                System.Diagnostics.Debug.WriteLine($"  Nombre: '{FilterNombre}'");
                System.Diagnostics.Debug.WriteLine($"  Tipo: '{FilterTipo}'");

                // Verificar si hay filtros aplicados (excluyendo "Todos" que representa sin filtro)
                bool hasFilters = !string.IsNullOrWhiteSpace(FilterNombre) ||
                                  (!string.IsNullOrEmpty(FilterTipo) && FilterTipo != "Todos");

                if (!hasFilters)
                {
                    // Sin filtros, cargar todos
                    await LoadEventsAsync();
                    return;
                }

                var filter = new EventoFilter
                {
                    nombre = string.IsNullOrWhiteSpace(FilterNombre) ? null : FilterNombre,
                    tipo = (string.IsNullOrEmpty(FilterTipo) || FilterTipo == "Todos") ? null : FilterTipo
                };

                System.Diagnostics.Debug.WriteLine($"Filtro creado:");
                System.Diagnostics.Debug.WriteLine($"  filter.nombre: '{filter.nombre}'");
                System.Diagnostics.Debug.WriteLine($"  filter.tipo: '{filter.tipo}'");

                var events = await _dataService.GetEventosFiltradosAsync(filter);

                System.Diagnostics.Debug.WriteLine($"Eventos encontrados: {events.Count}");

                // Debug de los eventos encontrados
                if (events.Count > 0)
                {
                    System.Diagnostics.Debug.WriteLine("Eventos encontrados:");
                    foreach (var eventItem in events.Take(5)) // Mostrar solo los primeros 5
                    {
                        System.Diagnostics.Debug.WriteLine($"  {eventItem.nombre}: Tipo='{eventItem.tipo}'");
                    }
                }
                else if (filter.nombre != null || filter.tipo != null)
                {
                    System.Diagnostics.Debug.WriteLine($"NO SE ENCONTRARON EVENTOS CON LOS FILTROS APLICADOS");
                }

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Eventos.Clear();
                    foreach (var eventItem in events)
                    {
                        Eventos.Add(eventItem);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al aplicar filtros: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
                System.Diagnostics.Debug.WriteLine($"Error en ApplyFiltersAsync: {ex}");
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task ClearFiltersAsync()
        {
            // Limpiar todos los filtros
            FilterNombre = string.Empty;
            FilterTipo = "Todos"; // Volver a "Todos"

            // Recargar todos los eventos
            await LoadEventsAsync();
        }

        private void AddEvent()
        {
            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de creación
                bool result = Views.EventFormWindow.ShowCreateDialog(mainWindow);

                // Si se creó el evento exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadEventsAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de creación: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void EditEvent(Evento eventItem)
        {
            if (eventItem == null) return;

            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de edición
                bool result = Views.EventFormWindow.ShowEditDialog(eventItem, mainWindow);

                // Si se editó el evento exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadEventsAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de edición: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task DeleteEventAsync(Evento eventItem)
        {
            if (eventItem == null) return;

            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas eliminar el evento '{eventItem.nombre}'?\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;
                    var success = await _dataService.DeleteEventoAsync(eventItem._id);

                    if (success)
                    {
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Eventos.Remove(eventItem);
                        });

                        UpdateStatusText();
                        MessageBox.Show("Evento eliminado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                    else
                    {
                        MessageBox.Show("Error al eliminar el evento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al eliminar evento: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        private void UpdateStatusText()
        {
            var count = Eventos?.Count ?? 0;
            StatusText = $"Mostrando {count} evento{(count != 1 ? "s" : "")}";
        }

        public async Task RefreshAsync()
        {
            await LoadEventsAsync();
        }
        #endregion

        #region INotifyPropertyChanged
        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        #endregion
    }
}