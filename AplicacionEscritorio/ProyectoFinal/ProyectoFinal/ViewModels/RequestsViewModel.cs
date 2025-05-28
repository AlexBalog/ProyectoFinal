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
using ProyectoFinal.Views;

namespace ProyectoFinal.ViewModels
{
    public class RequestsViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Entrenamiento> _peticiones;
        private Entrenamiento? _selectedRequest;
        private bool _isLoading;
        private string _statusText;

        // Filtros
        private string _filterNombre;
        private string _filterCreador;
        private string _filterCategoria;

        public RequestsViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            _peticiones = new ObservableCollection<Entrenamiento>();

            // Inicializar propiedades con valores por defecto
            _statusText = "Cargando...";
            _filterNombre = string.Empty;
            _filterCreador = string.Empty;
            _filterCategoria = "Todas";

            // Inicializar filtros con valores por defecto
            InitializeFilters();

            // Inicializar comandos
            InitializeCommands();

            // Cargar datos iniciales
            _ = LoadRequestsAsync();
        }

        #region Propiedades
        public ObservableCollection<Entrenamiento> Peticiones
        {
            get => _peticiones;
            set
            {
                if (_peticiones != value)
                {
                    _peticiones = value ?? throw new ArgumentNullException(nameof(value));
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Entrenamiento? SelectedRequest
        {
            get => _selectedRequest;
            set
            {
                if (_selectedRequest != value)
                {
                    _selectedRequest = value;
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
                    _statusText = value ?? string.Empty;
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
                    _filterNombre = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterCreador
        {
            get => _filterCreador;
            set
            {
                if (_filterCreador != value)
                {
                    _filterCreador = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterCategoria
        {
            get => _filterCategoria;
            set
            {
                if (_filterCategoria != value)
                {
                    _filterCategoria = value ?? "Todas";
                    OnPropertyChanged();
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; } = null!;
        public ICommand ClearFiltersCommand { get; private set; } = null!;
        public ICommand ViewDetailsCommand { get; private set; } = null!;
        public ICommand QuickApproveCommand { get; private set; } = null!;
        public ICommand QuickRejectCommand { get; private set; } = null!;
        public ICommand RefreshCommand { get; private set; } = null!;

        private void InitializeCommands()
        {
            ApplyFiltersCommand = new RelayCommand(async () => await ApplyFiltersAsync());
            ClearFiltersCommand = new RelayCommand(async () => await ClearFiltersAsync());
            ViewDetailsCommand = new RelayCommand<Entrenamiento>(request => ViewRequestDetails(request));
            QuickApproveCommand = new RelayCommand<Entrenamiento>(async request => await QuickApproveAsync(request));
            QuickRejectCommand = new RelayCommand<Entrenamiento>(async request => await QuickRejectAsync(request));
            RefreshCommand = new RelayCommand(async () => await LoadRequestsAsync());
        }
        #endregion

        #region Métodos
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterCreador = string.Empty;
            FilterCategoria = "Todas"; // Valor por defecto
        }

        public async Task LoadRequestsAsync()
        {
            try
            {
                IsLoading = true;

                // Filtrar entrenamientos con pedido = true y aprobado = false
                var filter = new EntrenamientoFilter
                {
                    pedido = true,
                    aprobado = false,
                    sortBy = "nombre",
                    sortDirection = "asc"
                };

                var requests = await _dataService.GetEntrenamientosFiltradosAsync(filter);

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Peticiones.Clear();
                    if (requests != null)
                    {
                        foreach (var request in requests)
                        {
                            Peticiones.Add(request);
                        }
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar peticiones: {ex.Message}", "Error",
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

                // Crear filtro base para peticiones pendientes
                var filter = new EntrenamientoFilter
                {
                    pedido = true,
                    aprobado = false,
                    sortBy = "nombre",
                    sortDirection = "asc"
                };

                // Agregar filtros adicionales si están especificados
                if (!string.IsNullOrWhiteSpace(FilterNombre))
                    filter.nombre = FilterNombre;

                if (!string.IsNullOrWhiteSpace(FilterCreador))
                    filter.creador = FilterCreador;

                if (!string.IsNullOrEmpty(FilterCategoria) && FilterCategoria != "Todas")
                    filter.categoria = FilterCategoria;

                var requests = await _dataService.GetEntrenamientosFiltradosAsync(filter);

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Peticiones.Clear();
                    if (requests != null)
                    {
                        foreach (var request in requests)
                        {
                            Peticiones.Add(request);
                        }
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al aplicar filtros: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
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
            FilterCreador = string.Empty;
            FilterCategoria = "Todas";

            // Recargar todas las peticiones
            await LoadRequestsAsync();
        }

        private void ViewRequestDetails(Entrenamiento? request)
        {
            if (request == null) return;

            try
            {
                var detailWindow = new TrainingRequestDetailWindow(request);
                /*if (Application.Current.MainWindow != null)
                {
                    detailWindow.Owner = Application.Current.MainWindow;
                }*/

                var result = detailWindow.ShowDialog();

                // Si se realizó alguna acción en la ventana de detalles, refrescar la lista
                if (result == true)
                {
                    _ = LoadRequestsAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir detalles: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task QuickApproveAsync(Entrenamiento? request)
        {
            if (request == null) return;

            var result = MessageBox.Show(
                $"¿Aprobar rápidamente el entrenamiento '{request.nombre}'?",
                "Confirmación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;

                    // Actualizar el entrenamiento
                    request.aprobado = true;
                    request.pedido = true;
                    request.motivoRechazo = null; // Limpiar motivo de rechazo si lo había

                    var success = await _dataService.UpdateEntrenamientoAsync(request);

                    if (success)
                    {
                        MessageBox.Show("¡Nuevo entrenamiento aprobado!", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);

                        // Remover de la lista de peticiones
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Peticiones.Remove(request);
                        });

                        UpdateStatusText();
                    }
                    else
                    {
                        MessageBox.Show("Error al aprobar el entrenamiento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al aprobar entrenamiento: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        private async Task QuickRejectAsync(Entrenamiento? request)
        {
            if (request == null) return;

            // Pedir motivo de rechazo
            string motivo = Microsoft.VisualBasic.Interaction.InputBox(
                "Ingrese el motivo del rechazo:",
                "Rechazar Entrenamiento",
                "");

            if (!string.IsNullOrWhiteSpace(motivo))
            {
                try
                {
                    IsLoading = true;

                    // Actualizar el entrenamiento
                    request.aprobado = false;
                    request.pedido = false;
                    request.motivoRechazo = motivo;

                    var success = await _dataService.UpdateEntrenamientoAsync(request);

                    if (success)
                    {
                        MessageBox.Show("Entrenamiento rechazado correctamente.", "Información",
                            MessageBoxButton.OK, MessageBoxImage.Information);

                        // Remover de la lista de peticiones
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Peticiones.Remove(request);
                        });

                        UpdateStatusText();
                    }
                    else
                    {
                        MessageBox.Show("Error al rechazar el entrenamiento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al rechazar entrenamiento: {ex.Message}", "Error",
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
            var count = Peticiones?.Count ?? 0;
            StatusText = $"Mostrando {count} petición{(count != 1 ? "es" : "")} pendiente{(count != 1 ? "s" : "")}";
        }

        public async Task RefreshAsync()
        {
            await LoadRequestsAsync();
        }
        #endregion

        #region INotifyPropertyChanged
        public event PropertyChangedEventHandler? PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string? propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
        #endregion
    }
}