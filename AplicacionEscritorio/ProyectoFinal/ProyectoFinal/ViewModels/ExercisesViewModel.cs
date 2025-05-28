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
    public class ExercisesViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Ejercicio> _ejercicios;
        private Ejercicio _selectedExercise;
        private bool _isLoading;
        private string _statusText;

        // Filtros
        private string _filterNombre;
        private string _filterMusculo;

        public ExercisesViewModel()
        {
            // Inicializar servicios
            _dataService = new DataService(new ApiService());

            // Inicializar colecciones
            Ejercicios = new ObservableCollection<Ejercicio>();

            // Inicializar filtros con valores por defecto
            InitializeFilters();

            // Inicializar comandos
            InitializeCommands();

            // Cargar datos iniciales
            _ = LoadExercisesAsync();
        }

        #region Propiedades
        public ObservableCollection<Ejercicio> Ejercicios
        {
            get => _ejercicios;
            set
            {
                if (_ejercicios != value)
                {
                    _ejercicios = value;
                    OnPropertyChanged();
                    UpdateStatusText();
                }
            }
        }

        public Ejercicio SelectedExercise
        {
            get => _selectedExercise;
            set
            {
                if (_selectedExercise != value)
                {
                    _selectedExercise = value;
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

        public string FilterMusculo
        {
            get => _filterMusculo;
            set
            {
                if (_filterMusculo != value)
                {
                    _filterMusculo = value;
                    OnPropertyChanged();
                    System.Diagnostics.Debug.WriteLine($"FilterMusculo changed to: '{_filterMusculo}'");
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand ApplyFiltersCommand { get; private set; }
        public ICommand ClearFiltersCommand { get; private set; }
        public ICommand AddExerciseCommand { get; private set; }
        public ICommand EditExerciseCommand { get; private set; }
        public ICommand DeleteExerciseCommand { get; private set; }
        public ICommand RefreshCommand { get; private set; }

        private void InitializeCommands()
        {
            ApplyFiltersCommand = new RelayCommand(async () => await ApplyFiltersAsync());
            ClearFiltersCommand = new RelayCommand(async () => await ClearFiltersAsync());
            AddExerciseCommand = new RelayCommand(() => AddExercise());
            EditExerciseCommand = new RelayCommand<Ejercicio>(exercise => EditExercise(exercise));
            DeleteExerciseCommand = new RelayCommand<Ejercicio>(async exercise => await DeleteExerciseAsync(exercise));
            RefreshCommand = new RelayCommand(async () => await LoadExercisesAsync());
        }
        #endregion

        #region Métodos
        private void InitializeFilters()
        {
            FilterNombre = string.Empty;
            FilterMusculo = "Todos"; // Valor por defecto
        }

        public async Task LoadExercisesAsync()
        {
            try
            {
                IsLoading = true;
                var exercises = await _dataService.GetAllEjerciciosAsync();

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Ejercicios.Clear();
                    foreach (var exercise in exercises)
                    {
                        Ejercicios.Add(exercise);
                    }
                });

                UpdateStatusText();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar ejercicios: {ex.Message}", "Error",
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
                System.Diagnostics.Debug.WriteLine($"Aplicando filtros de ejercicios:");
                System.Diagnostics.Debug.WriteLine($"  Nombre: '{FilterNombre}'");
                System.Diagnostics.Debug.WriteLine($"  Músculo: '{FilterMusculo}'");

                // Verificar si hay filtros aplicados (excluyendo "Todos" que representa sin filtro)
                bool hasFilters = !string.IsNullOrWhiteSpace(FilterNombre) ||
                                  (!string.IsNullOrEmpty(FilterMusculo) && FilterMusculo != "Todos");

                if (!hasFilters)
                {
                    // Sin filtros, cargar todos
                    await LoadExercisesAsync();
                    return;
                }

                var filter = new EjercicioFilter
                {
                    nombre = string.IsNullOrWhiteSpace(FilterNombre) ? null : FilterNombre,
                    musculo = (string.IsNullOrEmpty(FilterMusculo) || FilterMusculo == "Todos") ? null : FilterMusculo,
                    sortBy = "nombre",
                    sortDirection = "asc"
                };

                System.Diagnostics.Debug.WriteLine($"Filtro creado:");
                System.Diagnostics.Debug.WriteLine($"  filter.nombre: '{filter.nombre}'");
                System.Diagnostics.Debug.WriteLine($"  filter.musculo: '{filter.musculo}'");

                var exercises = await _dataService.GetEjerciciosFiltradosAsync(filter);

                System.Diagnostics.Debug.WriteLine($"Ejercicios encontrados: {exercises.Count}");

                // Debug de los ejercicios encontrados
                if (exercises.Count > 0)
                {
                    System.Diagnostics.Debug.WriteLine("Ejercicios encontrados:");
                    foreach (var exercise in exercises.Take(5)) // Mostrar solo los primeros 5
                    {
                        System.Diagnostics.Debug.WriteLine($"  {exercise.nombre}: Músculo='{exercise.musculo}'");
                    }
                }
                else if (filter.nombre != null || filter.musculo != null)
                {
                    System.Diagnostics.Debug.WriteLine($"NO SE ENCONTRARON EJERCICIOS CON LOS FILTROS APLICADOS");
                }

                Application.Current.Dispatcher.Invoke(() =>
                {
                    Ejercicios.Clear();
                    foreach (var exercise in exercises)
                    {
                        Ejercicios.Add(exercise);
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
            FilterMusculo = "Todos"; // Volver a "Todos"

            // Recargar todos los ejercicios
            await LoadExercisesAsync();
        }

        private void AddExercise()
        {
            MessageBox.Show("Función para agregar ejercicio", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
        }

        private void EditExercise(Ejercicio exercise)
        {
            if (exercise == null) return;

            MessageBox.Show($"Función para editar ejercicio: {exercise.nombre}", "Información",
                MessageBoxButton.OK, MessageBoxImage.Information);
        }

        private async Task DeleteExerciseAsync(Ejercicio exercise)
        {
            if (exercise == null) return;

            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas eliminar el ejercicio '{exercise.nombre}'?\n\nEsta acción no se puede deshacer.",
                "Confirmar Eliminación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Warning);

            if (result == MessageBoxResult.Yes)
            {
                try
                {
                    IsLoading = true;
                    var success = await _dataService.DeleteEjercicioAsync(exercise._id);

                    if (success)
                    {
                        Application.Current.Dispatcher.Invoke(() =>
                        {
                            Ejercicios.Remove(exercise);
                        });

                        UpdateStatusText();
                        MessageBox.Show("Ejercicio eliminado exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                    else
                    {
                        MessageBox.Show("Error al eliminar el ejercicio.", "Error",
                            MessageBoxButton.OK, MessageBoxImage.Error);
                    }
                }
                catch (Exception ex)
                {
                    MessageBox.Show($"Error al eliminar ejercicio: {ex.Message}", "Error",
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
            var count = Ejercicios?.Count ?? 0;
            StatusText = $"Mostrando {count} ejercicio{(count != 1 ? "s" : "")}";
        }

        public async Task RefreshAsync()
        {
            await LoadExercisesAsync();
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