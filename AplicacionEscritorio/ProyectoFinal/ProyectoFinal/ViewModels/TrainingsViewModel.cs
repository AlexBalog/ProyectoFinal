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

        public TrainingsViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            Entrenamientos = new ObservableCollection<Entrenamiento>();

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
        }
        #endregion

        #region Métodos
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

        private async Task ApplyFiltersAsync()
        {
            try
            {
                IsLoading = true;

                // Verificar si hay filtros aplicados
                if (string.IsNullOrWhiteSpace(FilterNombre) &&
                    string.IsNullOrWhiteSpace(FilterCategoria) &&
                    string.IsNullOrWhiteSpace(FilterMusculoPrincipal) &&
                    string.IsNullOrWhiteSpace(FilterDuracionMin) &&
                    string.IsNullOrWhiteSpace(FilterDuracionMax) &&
                    string.IsNullOrWhiteSpace(FilterCreador) &&
                    string.IsNullOrWhiteSpace(FilterEstado))
                {
                    await LoadTrainingsAsync();
                    return;
                }

                var filter = new EntrenamientoFilter
                {
                    nombre = FilterNombre,
                    categoria = FilterCategoria,
                    musculoPrincipal = FilterMusculoPrincipal,
                    creador = FilterCreador
                };

                // Manejar duración
                if (int.TryParse(FilterDuracionMin, out int durMin))
                    filter.duracionMin = durMin;
                if (int.TryParse(FilterDuracionMax, out int durMax))
                    filter.duracionMax = durMax;

                // Manejar estado
                if (!string.IsNullOrWhiteSpace(FilterEstado))
                {
                    switch (FilterEstado)
                    {
                        case "aprobado":
                            filter.aprobado = true;
                            break;
                        case "pendiente":
                            filter.pedido = true;
                            filter.aprobado = false;
                            break;
                        case "sin_solicitar":
                            filter.pedido = false;
                            break;
                    }
                }

                var trainings = await _dataService.GetEntrenamientosFiltradosAsync(filter);

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
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task ClearFiltersAsync()
        {
            FilterNombre = string.Empty;
            FilterCategoria = string.Empty;
            FilterMusculoPrincipal = string.Empty;
            FilterDuracionMin = string.Empty;
            FilterDuracionMax = string.Empty;
            FilterCreador = string.Empty;
            FilterEstado = string.Empty;

            await LoadTrainingsAsync();
        }

        private void AddTraining()
        {
            MessageBox.Show("Función para agregar entrenamiento", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
        }

        private void EditTraining(Entrenamiento training)
        {
            if (training == null) return;

            MessageBox.Show($"Función para editar entrenamiento: {training.nombre}", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
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