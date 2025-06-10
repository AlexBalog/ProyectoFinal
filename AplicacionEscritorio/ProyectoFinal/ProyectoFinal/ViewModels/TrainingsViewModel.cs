using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.Views;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class TrainingsViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Entrenamiento> _entrenamientos;
        private Entrenamiento _selectedTraining;
        private bool _isLoading;
        private string _statusText;

        // Filtros
        private string _filterNombre;
        private string _filterCategoria;
        private string _filterMusculoPrincipal;
        private string _filterDuracionMin;
        private string _filterDuracionMax;
        private string _filterCreador;
        private string _filterEstado;
        private string _filterBaja;

        public TrainingsViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            Entrenamientos = new ObservableCollection<Entrenamiento>();

            // Inicializar filtros con valores por defecto
            InitializeFilters();

            // Inicializar comandos
            InitializeCommands();

            // Cargar datos iniciales
            _ = LoadTrainingsAsync();
        }

        #region Propiedades
        public ObservableCollection<Entrenamiento> Entrenamientos
        {
            get => _entrenamientos;
            set
            {
                if (_entrenamientos != value)
                {
                    _entrenamientos = value;
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Entrenamiento SelectedTraining
        {
            get => _selectedTraining;
            set
            {
                if (_selectedTraining != value)
                {
                    _selectedTraining = value;
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

        public string FilterCategoria
        {
            get => _filterCategoria;
            set
            {
                if (_filterCategoria != value)
                {
                    _filterCategoria = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterCategoria changed to: '{_filterCategoria}'");
                }
            }
        }

        public string FilterMusculoPrincipal
        {
            get => _filterMusculoPrincipal;
            set
            {
                if (_filterMusculoPrincipal != value)
                {
                    _filterMusculoPrincipal = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterMusculoPrincipal changed to: '{_filterMusculoPrincipal}'");
                }
            }
        }

        public string FilterDuracionMin
        {
            get => _filterDuracionMin;
            set
            {
                if (_filterDuracionMin != value)
                {
                    _filterDuracionMin = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterDuracionMax
        {
            get => _filterDuracionMax;
            set
            {
                if (_filterDuracionMax != value)
                {
                    _filterDuracionMax = value;
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
                    _filterCreador = value;
                    OnPropertyChanged();
                }
            }
        }

        public string FilterEstado
        {
            get => _filterEstado;
            set
            {
                if (_filterEstado != value)
                {
                    _filterEstado = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterEstado changed to: '{_filterEstado}'");
                }
            }
        }

        public string FilterBaja
        {
            get => _filterBaja;
            set
            {
                if (_filterBaja != value)
                {
                    _filterBaja = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterBaja changed to: '{_filterBaja}'");
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; }
        public ICommand ClearFiltersCommand { get; private set; }
        public ICommand AddTrainingCommand { get; private set; }
        public ICommand EditTrainingCommand { get; private set; }
        public ICommand DeleteTrainingCommand { get; private set; }
        public ICommand ApproveTrainingCommand { get; private set; }
        public ICommand RejectTrainingCommand { get; private set; }
        public ICommand RefreshCommand { get; private set; }
        public ICommand DeactivateTrainingCommand { get; private set; }
        public ICommand ReactivateTrainingCommand { get; private set; }


        private void InitializeCommands()
        {
            ApplyFiltersCommand = new RelayCommand(async () => await ApplyFiltersAsync());
            ClearFiltersCommand = new RelayCommand(async () => await ClearFiltersAsync());
            AddTrainingCommand = new RelayCommand(() => AddTraining());
            EditTrainingCommand = new RelayCommand<Entrenamiento>(training => EditTraining(training));
            DeleteTrainingCommand = new RelayCommand<Entrenamiento>(async training => await DeleteTrainingAsync(training));
            ApproveTrainingCommand = new RelayCommand<Entrenamiento>(async training => await ApproveTrainingAsync(training));
            RejectTrainingCommand = new RelayCommand<Entrenamiento>(async training => await RejectTrainingAsync(training));
            RefreshCommand = new RelayCommand(async () => await LoadTrainingsAsync());
            DeactivateTrainingCommand = new RelayCommand<Entrenamiento>(async training => await DeactivateTrainingAsync(training));
            ReactivateTrainingCommand = new RelayCommand<Entrenamiento>(async training => await ReactivateTrainingAsync(training));
        }
        #endregion

        #region Métodos
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterCategoria = "Todas"; // Valor por defecto
            FilterMusculoPrincipal = "Todos"; // Valor por defecto
            FilterDuracionMin = string.Empty;
            FilterDuracionMax = string.Empty;
            FilterCreador = string.Empty;
            FilterEstado = "Todos"; // Valor por defecto
            FilterBaja = "Todos";
        }

        public async Task LoadTrainingsAsync()
        {
            try
            {
                IsLoading = true;
                var trainings = await _dataService.GetAllEntrenamientosAsync();

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Entrenamientos.Clear();
                    foreach (var training in trainings)
                    {
                        Entrenamientos.Add(training);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar entrenamientos: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task DeactivateTrainingAsync(Entrenamiento training)
        {
            if (training == null) return;

            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas dar de baja el entrenamiento '{training.nombre}'?\n\nEsta acción se puede revertir posteriormente.",
                "Confirmar Baja",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;

                    // Actualizar el entrenamiento
                    training.baja = true;
                    training.fechaBaja = DateTime.Now;

                    var success = await _dataService.UpdateEntrenamientoAsync(training);

                    if (success)
                    {
                        MessageBox.Show("Entrenamiento dado de baja exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                        await LoadTrainingsAsync(); // Recargar para actualizar vista
                    }
                    else
                    {
                        MessageBox.Show("Error al dar de baja el entrenamiento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al dar de baja entrenamiento: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        // Implementar método para reactivar
        private async Task ReactivateTrainingAsync(Entrenamiento training)
        {
            if (training == null) return;

            var result = MessageBox.Show(
                $"¿Reactivar el entrenamiento '{training.nombre}'?",
                "Confirmar Reactivación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;

                    // Actualizar el entrenamiento
                    training.baja = false;
                    training.fechaBaja = null; // Limpiar fecha de baja

                    var success = await _dataService.UpdateEntrenamientoAsync(training);

                    if (success)
                    {
                        MessageBox.Show("Entrenamiento reactivado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                        await LoadTrainingsAsync(); // Recargar para actualizar vista
                    }
                    else
                    {
                        MessageBox.Show("Error al reactivar el entrenamiento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al reactivar entrenamiento: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        private async Task ApplyFiltersAsync()
        {
            try
            {
                IsLoading = true;

                // Debug: mostrar valores de filtros
                System.Diagnostics.Debug.WriteLine($"Aplicando filtros de entrenamientos:");
                System.Diagnostics.Debug.WriteLine($"  Nombre: '{FilterNombre}'");
                System.Diagnostics.Debug.WriteLine($"  Categoría: '{FilterCategoria}'");
                System.Diagnostics.Debug.WriteLine($"  Músculo Principal: '{FilterMusculoPrincipal}'");
                System.Diagnostics.Debug.WriteLine($"  Duración Min: '{FilterDuracionMin}'");
                System.Diagnostics.Debug.WriteLine($"  Duración Max: '{FilterDuracionMax}'");
                System.Diagnostics.Debug.WriteLine($"  Creador: '{FilterCreador}'");
                System.Diagnostics.Debug.WriteLine($"  Estado: '{FilterEstado}'");
                System.Diagnostics.Debug.WriteLine($"  Baja: '{FilterBaja}'");

                // Verificar si hay filtros aplicados (excluyendo "Todas/Todos" que representa sin filtro)
                bool hasFilters = !string.IsNullOrWhiteSpace(FilterNombre) ||
                                  (!string.IsNullOrEmpty(FilterCategoria) && FilterCategoria != "Todas") ||
                                  (!string.IsNullOrEmpty(FilterMusculoPrincipal) && FilterMusculoPrincipal != "Todos") ||
                                  !string.IsNullOrWhiteSpace(FilterDuracionMin) ||
                                  !string.IsNullOrWhiteSpace(FilterDuracionMax) ||
                                  !string.IsNullOrWhiteSpace(FilterCreador) ||
                                  (!string.IsNullOrEmpty(FilterEstado) && FilterEstado != "Todos") ||
                                  (!string.IsNullOrEmpty(FilterBaja) && FilterBaja != "Todos");

                if (!hasFilters)
                {
                    // Sin filtros, cargar todos
                    await LoadTrainingsAsync();
                    return;
                }

                var filter = new EntrenamientoFilter
                {
                    nombre = string.IsNullOrWhiteSpace(FilterNombre) ? null : FilterNombre,
                    categoria = (string.IsNullOrEmpty(FilterCategoria) || FilterCategoria == "Todas") ? null : FilterCategoria,
                    musculoPrincipal = (string.IsNullOrEmpty(FilterMusculoPrincipal) || FilterMusculoPrincipal == "Todos") ? null : FilterMusculoPrincipal,
                    creador = string.IsNullOrWhiteSpace(FilterCreador) ? null : FilterCreador
                };

                // Manejar duración
                if (int.TryParse(FilterDuracionMin, out int durMin))
                    filter.duracionMin = durMin;
                if (int.TryParse(FilterDuracionMax, out int durMax))
                    filter.duracionMax = durMax;

                // Manejar estado
                if (!string.IsNullOrEmpty(FilterEstado) && FilterEstado != "Todos")
                {
                    switch (FilterEstado)
                    {
                        case "Aprobados":
                            filter.aprobado = true;
                            break;
                        case "Pendientes":
                            filter.pedido = true;
                            filter.aprobado = false;
                            break;
                        case "Sin solicitar":
                            filter.pedido = false;
                            break;
                    }
                }

                // Manejar baja
                if (!string.IsNullOrEmpty(FilterBaja) && FilterBaja != "Todos")
                {
                    switch (FilterBaja)
                    {
                        case "Activos":
                            filter.baja = false;
                            break;
                        case "Dados de baja":
                            filter.baja = true;
                            break;
                    }
                }

                System.Diagnostics.Debug.WriteLine($"Filtro creado:");
                System.Diagnostics.Debug.WriteLine($"  filter.nombre: '{filter.nombre}'");
                System.Diagnostics.Debug.WriteLine($"  filter.categoria: '{filter.categoria}'");
                System.Diagnostics.Debug.WriteLine($"  filter.musculoPrincipal: '{filter.musculoPrincipal}'");
                System.Diagnostics.Debug.WriteLine($"  filter.creador: '{filter.creador}'");
                System.Diagnostics.Debug.WriteLine($"  filter.duracionMin: '{filter.duracionMin}'");
                System.Diagnostics.Debug.WriteLine($"  filter.duracionMax: '{filter.duracionMax}'");
                System.Diagnostics.Debug.WriteLine($"  filter.aprobado: '{filter.aprobado}'");
                System.Diagnostics.Debug.WriteLine($"  filter.pedido: '{filter.pedido}'");
                System.Diagnostics.Debug.WriteLine($"  filter.baja: '{filter.baja}'");

                var trainings = await _dataService.GetEntrenamientosFiltradosAsync(filter);

                System.Diagnostics.Debug.WriteLine($"Entrenamientos encontrados: {trainings.Count}");

                // Debug de los entrenamientos encontrados
                if (trainings.Count > 0)
                {
                    System.Diagnostics.Debug.WriteLine("Entrenamientos encontrados:");
                    foreach (var training in trainings.Take(5)) // Mostrar solo los primeros 5
                    {
                        System.Diagnostics.Debug.WriteLine($"  {training.nombre}: Categoría='{training.categoria}', Músculo='{training.musculoPrincipal}', Estado='{training.EstadoAprobacion}'");
                    }
                }
                else if (filter.categoria != null || filter.musculoPrincipal != null || filter.aprobado != null)
                {
                    System.Diagnostics.Debug.WriteLine($"NO SE ENCONTRARON ENTRENAMIENTOS CON LOS FILTROS APLICADOS");
                }

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Entrenamientos.Clear();
                    foreach (var training in trainings)
                    {
                        Entrenamientos.Add(training);
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
            FilterCategoria = "Todas"; // Volver a "Todas"
            FilterMusculoPrincipal = "Todos"; // Volver a "Todos"
            FilterDuracionMin = string.Empty;
            FilterDuracionMax = string.Empty;
            FilterCreador = string.Empty;
            FilterEstado = "Todos"; // Volver a "Todos"
            FilterBaja = "Todos";

            // Recargar todos los entrenamientos
            await LoadTrainingsAsync();
        }

        private void AddTraining()
        {
            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de creación
                bool result = Views.TrainingFormWindow.ShowCreateDialog(mainWindow);

                // Si se creó el entrenamiento exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadTrainingsAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de creación: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void EditTraining(Entrenamiento? training)
        {
            if (training == null) return;

            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Mostrar la ventana de edición
                bool result = Views.TrainingFormWindow.ShowEditDialog(training, mainWindow);

                // Si se editó el entrenamiento exitosamente, refrescar la lista
                if (result)
                {
                    _ = LoadTrainingsAsync();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al abrir ventana de edición: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private async Task DeleteTrainingAsync(Entrenamiento training)
        {
            if (training == null) return;

            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas eliminar el entrenamiento '{training.nombre}'?\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;
                    var success = await _dataService.DeleteEntrenamientoAsync(training._id);

                    if (success)
                    {
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Entrenamientos.Remove(training);
                        });

                        UpdateStatusText();
                        MessageBox.Show("Entrenamiento eliminado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                    else
                    {
                        MessageBox.Show("Error al eliminar el entrenamiento.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al eliminar entrenamiento: {ex.Message}", "Error",
                        MessageBoxButton.OK, MessageBoxImage.Error);
                }
                finally
                {
                    IsLoading = false;
                }
            }
        }

        private async Task ApproveTrainingAsync(Entrenamiento training)
        {
            if (training == null) return;

            var result = MessageBox.Show(
                $"¿Aprobar el entrenamiento '{training.nombre}'?",
                "Confirmar Aprobación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;
                    training.aprobado = true;
                    training.pedido = true;

                    var success = await _dataService.UpdateEntrenamientoAsync(training);

                    if (success)
                    {
                        MessageBox.Show("Entrenamiento aprobado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                        await LoadTrainingsAsync(); // Recargar para actualizar vista
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

        private async Task RejectTrainingAsync(Entrenamiento training)
        {
            if (training == null) return;

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
                    training.aprobado = false;
                    training.pedido = false;
                    training.motivoRechazo = motivo;

                    var success = await _dataService.UpdateEntrenamientoAsync(training);

                    if (success)
                    {
                        MessageBox.Show("Entrenamiento rechazado.", "Información",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                        await LoadTrainingsAsync(); // Recargar para actualizar vista
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
            var count = Entrenamientos?.Count ?? 0;
            StatusText = $"Mostrando {count} entrenamiento{(count != 1 ? "s" : "")}";
        }

        public async Task RefreshAsync()
        {
            await LoadTrainingsAsync();
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