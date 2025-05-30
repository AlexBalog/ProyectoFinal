using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class EventFormViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private Evento? _originalEvent;
        private bool _isEditMode;
        private bool _isLoading;
        private string _windowTitle;

        // Campos del evento
        private string _id;
        private string _nombre;
        private string _descripcion;
        private string _tipo;

        public EventFormViewModel()
        {
            _dataService = new DataService(new ApiService());
            InitializeCommands();
            InitializeDefaults();
        }

        #region Propiedades
        public bool IsEditMode
        {
            get => _isEditMode;
            set
            {
                if (_isEditMode != value)
                {
                    _isEditMode = value;
                    OnPropertyChanged();
                    UpdateWindowTitle();
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
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string WindowTitle
        {
            get => _windowTitle ?? string.Empty;
            set
            {
                if (_windowTitle != value)
                {
                    _windowTitle = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        // Campos del formulario
        public string Id
        {
            get => _id ?? string.Empty;
            set
            {
                if (_id != value)
                {
                    _id = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public string Nombre
        {
            get => _nombre ?? string.Empty;
            set
            {
                if (_nombre != value)
                {
                    _nombre = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string Descripcion
        {
            get => _descripcion ?? string.Empty;
            set
            {
                if (_descripcion != value)
                {
                    _descripcion = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public string Tipo
        {
            get => _tipo ?? "Entrenamiento";
            set
            {
                if (_tipo != value)
                {
                    _tipo = value ?? "Entrenamiento";
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }
        #endregion

        #region Comandos
        public ICommand SaveCommand { get; private set; } = null!;
        public ICommand CancelCommand { get; private set; } = null!;

        private void InitializeCommands()
        {
            SaveCommand = new RelayCommand(async () => await SaveEventAsync(), CanSave);
            CancelCommand = new RelayCommand(() => RequestClose?.Invoke(false));
        }

        private bool CanSave()
        {
            if (IsLoading) return false;

            // Validaciones básicas
            if (string.IsNullOrWhiteSpace(Nombre) || string.IsNullOrWhiteSpace(Tipo))
            {
                return false;
            }

            return true;
        }
        #endregion

        #region Métodos públicos
        public void LoadEvent(Evento evento)
        {
            if (evento == null) return;

            _originalEvent = evento;
            IsEditMode = true;

            // Cargar datos del evento
            Id = evento._id;
            Nombre = evento.nombre;
            Descripcion = evento.descripcion ?? string.Empty;
            Tipo = evento.tipo ?? "Entrenamiento";
        }

        public void SetCreateMode()
        {
            IsEditMode = false;
            _originalEvent = null;
            InitializeDefaults();
        }
        #endregion

        #region Métodos privados
        private void InitializeDefaults()
        {
            Id = string.Empty;
            Nombre = string.Empty;
            Descripcion = string.Empty;
            Tipo = "Entrenamiento";

            UpdateWindowTitle();
        }

        private void UpdateWindowTitle()
        {
            WindowTitle = IsEditMode ? "Editar Evento" : "Crear Evento";
        }

        private async Task SaveEventAsync()
        {
            try
            {
                IsLoading = true;

                if (IsEditMode)
                {
                    await UpdateEventAsync();
                }
                else
                {
                    await CreateEventAsync();
                }

                MessageBox.Show(
                    IsEditMode ? "Evento actualizado exitosamente." : "Evento creado exitosamente.",
                    "Éxito",
                    MessageBoxButton.OK,
                    MessageBoxImage.Information);

                RequestClose?.Invoke(true);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    $"Error al {(IsEditMode ? "actualizar" : "crear")} evento: {ex.Message}",
                    "Error",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task CreateEventAsync()
        {
            var newEvent = new Evento
            {
                nombre = Nombre,
                descripcion = Descripcion,
                tipo = Tipo
            };

            var result = await _dataService.CreateEventoAsync(newEvent);
            if (result == null)
            {
                throw new Exception("No se pudo crear el evento");
            }
        }

        private async Task UpdateEventAsync()
        {
            if (_originalEvent == null) return;

            // Crear evento con los datos actualizados
            var updatedEvent = new Evento
            {
                _id = Id,
                nombre = Nombre,
                descripcion = Descripcion,
                tipo = Tipo
            };

            var success = await _dataService.UpdateEventoAsync(updatedEvent);
            if (!success)
            {
                throw new Exception("No se pudo actualizar el evento");
            }
        }
        #endregion

        #region Eventos
        public Action<bool>? RequestClose { get; set; }
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