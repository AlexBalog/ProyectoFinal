using System;
using System.Collections.Generic;
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
    public class ExerciseSelectionViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private ObservableCollection<Ejercicio> _exercises;
        private ObservableCollection<Ejercicio> _filteredExercises;
        private Ejercicio _selectedExercise;
        private bool _isLoading;
        private string _searchText;
        private string _filterMusculo;
        private List<string> _alreadySelectedIds;

        public ExerciseSelectionViewModel(List<string> alreadySelectedIds = null)
        {
            _dataService = new DataService(new ApiService());
            _exercises = new ObservableCollection<Ejercicio>();
            _filteredExercises = new ObservableCollection<Ejercicio>();
            _alreadySelectedIds = alreadySelectedIds ?? new List<string>();
            _searchText = string.Empty;
            _filterMusculo = "Todos";

            InitializeCommands();
            _ = LoadExercisesAsync();
        }

        #region Propiedades
        public ObservableCollection<Ejercicio> FilteredExercises
        {
            get => _filteredExercises;
            set
            {
                if (_filteredExercises != value)
                {
                    _filteredExercises = value;
                    OnPropertyChanged();
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
                    OnPropertyChanged(nameof(HasSelectedExercise)); // AGREGADO: Notificar cambio para la propiedad booleana
                    ((RelayCommand)SelectCommand).RaiseCanExecuteChanged();
                }
            }
        }

        // AGREGADO: Propiedad booleana para los bindings de visibilidad
        public bool HasSelectedExercise => SelectedExercise != null;

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

        public string SearchText
        {
            get => _searchText;
            set
            {
                if (_searchText != value)
                {
                    _searchText = value;
                    OnPropertyChanged();
                    // Aplicar filtros inmediatamente cuando cambia el texto de búsqueda
                    Application.Current.Dispatcher.BeginInvoke(new Action(() => ApplyFilters()));
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
                    // Aplicar filtros inmediatamente cuando cambia el filtro de músculo
                    Application.Current.Dispatcher.BeginInvoke(new Action(() => ApplyFilters()));
                }
            }
        }

        public List<string> MusculosList { get; } = new List<string>
        {
            "Todos", "Pecho", "Espalda", "Piernas", "Brazos", "Hombros", "Core", "Glúteos", "Cuádriceps", "Isquiotibiales", "Pantorrillas"
        };

        public string StatusText => $"Mostrando {FilteredExercises.Count} ejercicios";
        #endregion

        #region Comandos
        public ICommand SelectCommand { get; private set; }
        public ICommand CancelCommand { get; private set; }
        public ICommand RefreshCommand { get; private set; }

        private void InitializeCommands()
        {
            SelectCommand = new RelayCommand(() => RequestClose?.Invoke(SelectedExercise), CanSelect);
            CancelCommand = new RelayCommand(() => RequestClose?.Invoke(null));
            RefreshCommand = new RelayCommand(async () => await LoadExercisesAsync());
        }

        private bool CanSelect()
        {
            return SelectedExercise != null && !_alreadySelectedIds.Contains(SelectedExercise._id);
        }
        #endregion

        #region Métodos
        private async Task LoadExercisesAsync()
        {
            try
            {
                IsLoading = true;
                var exercises = await _dataService.GetAllEjerciciosAsync();

                Application.Current.Dispatcher.Invoke(() =>
                {
                    _exercises.Clear();
                    foreach (var exercise in exercises.OrderBy(e => e.nombre))
                    {
                        _exercises.Add(exercise);
                    }
                    ApplyFilters();
                });
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

        private void ApplyFilters()
        {
            var filtered = _exercises.AsEnumerable();

            // Filtrar por texto de búsqueda
            if (!string.IsNullOrWhiteSpace(SearchText))
            {
                var searchLower = SearchText.ToLower();
                filtered = filtered.Where(e =>
                    e.nombre.ToLower().Contains(searchLower) ||
                    e.descripcion?.ToLower().Contains(searchLower) == true ||
                    e.musculo?.ToLower().Contains(searchLower) == true);
            }

            // Filtrar por músculo
            if (!string.IsNullOrEmpty(FilterMusculo) && FilterMusculo != "Todos")
            {
                filtered = filtered.Where(e => e.musculo?.Equals(FilterMusculo, StringComparison.OrdinalIgnoreCase) == true);
            }

            // Actualizar la colección filtrada
            Application.Current.Dispatcher.Invoke(() =>
            {
                FilteredExercises.Clear();
                foreach (var exercise in filtered)
                {
                    FilteredExercises.Add(exercise);
                }
                OnPropertyChanged(nameof(StatusText));
            });
        }
        #endregion

        #region Eventos
        public Action<Ejercicio> RequestClose { get; set; }
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