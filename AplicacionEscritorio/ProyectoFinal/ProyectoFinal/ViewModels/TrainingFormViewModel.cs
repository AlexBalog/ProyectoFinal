using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using System.Text.RegularExpressions;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;
using ProyectoFinal.Views;

namespace ProyectoFinal.ViewModels
{
    public class TrainingFormViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly IUserService _userService; // NUEVO: Servicio para obtener datos del usuario
        private Entrenamiento? _originalTraining;
        private bool _isEditMode;
        private bool _isLoading;
        private string _windowTitle;

        // Campos del entrenamiento
        private string _id;
        private string _nombre;
        private string _categoria;
        private string _musculoPrincipal;
        private int _duracion;
        private string _foto;
        private ObservableCollection<string> _musculo;
        private int _likes;
        private ObservableCollection<string> _ejercicios;
        private string _creador;
        private bool _aprobado;
        private bool _pedido;
        private string _motivoRechazo;

        // Campos auxiliares para la UI
        private string _musculoTemp;
        private ObservableCollection<Ejercicio> _ejerciciosDetallados;

        public TrainingFormViewModel()
        {
            _dataService = new DataService(new ApiService());
            _userService = new UserService(); // NUEVO: Inicializar servicio de usuario
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
                    OnPropertyChanged(nameof(IsCreadorEditable)); // NUEVO: Notificar cambio en editabilidad
                    OnPropertyChanged(nameof(IsCreadorVisible)); // NUEVO: Notificar cambio en visibilidad
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

        public string Categoria
        {
            get => _categoria ?? "Fuerza";
            set
            {
                if (_categoria != value)
                {
                    _categoria = value ?? "Fuerza";
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string MusculoPrincipal
        {
            get => _musculoPrincipal ?? "Pecho";
            set
            {
                if (_musculoPrincipal != value)
                {
                    _musculoPrincipal = value ?? "Pecho";
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public int Duracion
        {
            get => _duracion;
            set
            {
                if (_duracion != value)
                {
                    _duracion = value;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string Foto
        {
            get => _foto ?? string.Empty;
            set
            {
                if (_foto != value)
                {
                    _foto = value ?? string.Empty;
                    OnPropertyChanged();
                    OnPropertyChanged(nameof(HasImage));
                }
            }
        }

        public ObservableCollection<string> Musculo
        {
            get => _musculo ??= new ObservableCollection<string>();
            set
            {
                if (_musculo != value)
                {
                    _musculo = value ?? new ObservableCollection<string>();
                    OnPropertyChanged();
                }
            }
        }

        public int Likes
        {
            get => _likes;
            set
            {
                if (_likes != value)
                {
                    _likes = value;
                    OnPropertyChanged();
                }
            }
        }

        public ObservableCollection<string> Ejercicios
        {
            get => _ejercicios ??= new ObservableCollection<string>();
            set
            {
                if (_ejercicios != value)
                {
                    _ejercicios = value ?? new ObservableCollection<string>();
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        // Nueva propiedad para ejercicios con detalles completos
        public ObservableCollection<Ejercicio> EjerciciosDetallados
        {
            get => _ejerciciosDetallados ??= new ObservableCollection<Ejercicio>();
            set
            {
                if (_ejerciciosDetallados != value)
                {
                    _ejerciciosDetallados = value ?? new ObservableCollection<Ejercicio>();
                    OnPropertyChanged();
                }
            }
        }

        public string Creador
        {
            get => _creador ?? string.Empty;
            set
            {
                if (_creador != value)
                {
                    _creador = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        // NUEVAS PROPIEDADES: Para controlar la visibilidad y editabilidad del campo creador
        public bool IsCreadorVisible => IsEditMode; // Solo visible en modo edición
        public bool IsCreadorEditable => false; // Nunca editable (siempre readonly)

        public bool Aprobado
        {
            get => _aprobado;
            set
            {
                if (_aprobado != value)
                {
                    _aprobado = value;
                    OnPropertyChanged();
                }
            }
        }

        public bool Pedido
        {
            get => _pedido;
            set
            {
                if (_pedido != value)
                {
                    _pedido = value;
                    OnPropertyChanged();
                }
            }
        }

        public string MotivoRechazo
        {
            get => _motivoRechazo ?? string.Empty;
            set
            {
                if (_motivoRechazo != value)
                {
                    _motivoRechazo = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        // Propiedades auxiliares para la UI
        public string MusculoTemp
        {
            get => _musculoTemp ?? string.Empty;
            set
            {
                if (_musculoTemp != value)
                {
                    _musculoTemp = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)AddMusculoCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public bool HasImage => !string.IsNullOrEmpty(Foto);

        // Listas para ComboBox
        public List<string> CategoriasList { get; } = new List<string>
        {
            "Fuerza", "Cardio", "Flexibilidad", "Resistencia", "Hipertrofia", "Fullbody"
        };

        public List<string> MusculosList { get; } = new List<string>
        {
            "Pecho", "Espalda", "Cuadriceps", "Biceps", "Hombros", "Triceps", "Gluteos", "Femoral", "Gemelos", "Abdominales", "Lumbares", "Antebrazos", "Aductores", "Abductores"
        };
        #endregion

        #region Comandos
        public ICommand SaveCommand { get; private set; } = null!;
        public ICommand CancelCommand { get; private set; } = null!;
        public ICommand SelectImageCommand { get; private set; } = null!;
        public ICommand RemoveImageCommand { get; private set; } = null!;
        public ICommand SelectEjercicioCommand { get; private set; } = null!;
        public ICommand RemoveEjercicioCommand { get; private set; } = null!;
        public ICommand AddMusculoCommand { get; private set; } = null!;
        public ICommand RemoveMusculoCommand { get; private set; } = null!;

        private void InitializeCommands()
        {
            SaveCommand = new RelayCommand(async () => await SaveTrainingAsync(), CanSave);
            CancelCommand = new RelayCommand(() => RequestClose?.Invoke(false));
            SelectImageCommand = new RelayCommand(() => SelectImage());
            RemoveImageCommand = new RelayCommand(() => RemoveImage());
            SelectEjercicioCommand = new RelayCommand(() => SelectEjercicio());
            RemoveEjercicioCommand = new RelayCommand<Ejercicio>(ejercicio => RemoveEjercicio(ejercicio));
            AddMusculoCommand = new RelayCommand(() => AddMusculo(), CanAddMusculo);
            RemoveMusculoCommand = new RelayCommand<string>(musculo => RemoveMusculo(musculo));
        }

        private bool CanSave()
        {
            if (IsLoading) return false;

            // Validaciones básicas - El creador se establece automáticamente, no necesita validación manual
            return !string.IsNullOrWhiteSpace(Nombre) &&
                   !string.IsNullOrWhiteSpace(Categoria) &&
                   !string.IsNullOrWhiteSpace(MusculoPrincipal) &&
                   Duracion > 0 &&
                   EjerciciosDetallados.Count > 0;
        }

        private bool CanAddMusculo()
        {
            return !string.IsNullOrWhiteSpace(MusculoTemp) &&
                   !Musculo.Contains(MusculoTemp.Trim());
        }
        #endregion

        #region Métodos públicos
        public void LoadTraining(Entrenamiento training)
        {
            if (training == null) return;

            _originalTraining = training;
            IsEditMode = true;

            // Cargar datos del entrenamiento
            Id = training._id;
            Nombre = training.nombre;
            Categoria = training.categoria;
            MusculoPrincipal = training.musculoPrincipal;
            Duracion = training.duracion;
            Foto = training.foto;
            Likes = training.likes;
            Creador = training.creador;
            Aprobado = training.aprobado;
            Pedido = training.pedido;
            MotivoRechazo = training.motivoRechazo;

            // Cargar listas
            Musculo.Clear();
            if (training.musculo != null)
            {
                foreach (var musculo in training.musculo)
                {
                    Musculo.Add(musculo);
                }
            }

            EjerciciosDetallados.Clear();
            Ejercicios.Clear();
            if (training.ejercicios != null)
            {
                foreach (var ejercicioId in training.ejercicios)
                {
                    Ejercicios.Add(ejercicioId);
                }
                // Cargar los detalles de los ejercicios
                _ = LoadEjerciciosDetallados();
            }
        }

        public void SetCreateMode()
        {
            IsEditMode = false;
            _originalTraining = null;
            InitializeDefaults();
            // NUEVO: Establecer automáticamente el creador al usuario logueado
            SetCurrentUserAsCreator();
        }
        #endregion

        #region Métodos privados
        private void InitializeDefaults()
        {
            Id = string.Empty;
            Nombre = string.Empty;
            Categoria = "Fuerza";
            MusculoPrincipal = "Pecho";
            Duracion = 30;
            Foto = string.Empty;
            Likes = 0;
            Creador = string.Empty;
            Aprobado = false;
            Pedido = false;
            MotivoRechazo = string.Empty;
            MusculoTemp = string.Empty;

            Musculo.Clear();
            Ejercicios.Clear();
            EjerciciosDetallados.Clear();

            UpdateWindowTitle();
        }

        // NUEVO MÉTODO: Establecer el usuario actual como creador
        private void SetCurrentUserAsCreator()
        {
            try
            {
                var userData = _userService.GetUserData();
                if (userData != null && !string.IsNullOrEmpty(userData.Id))
                {
                    Creador = userData.Id;
                    System.Diagnostics.Debug.WriteLine($"Creador establecido automáticamente: {Creador}");
                }
                else
                {
                    System.Diagnostics.Debug.WriteLine("No se pudo obtener el ID del usuario logueado");
                    // Fallback: intentar obtener desde token u otra fuente
                    Creador = "ADMIN"; // Valor por defecto temporal
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error al obtener usuario actual: {ex.Message}");
                Creador = "ADMIN"; // Valor por defecto en caso de error
            }
        }

        private void UpdateWindowTitle()
        {
            WindowTitle = IsEditMode ? "Editar Entrenamiento" : "Crear Entrenamiento";
        }

        private void SelectEjercicio()
        {
            try
            {
                // Obtener la ventana principal para usar como Owner
                var mainWindow = Application.Current.MainWindow;

                // Obtener los IDs de ejercicios ya seleccionados
                var alreadySelectedIds = Ejercicios.ToList();

                // Mostrar la ventana de selección
                var selectedExercise = ExerciseSelectionWindow.ShowSelectionDialog(alreadySelectedIds, mainWindow);

                if (selectedExercise != null)
                {
                    // Agregar el ejercicio a las listas
                    Ejercicios.Add(selectedExercise._id);
                    EjerciciosDetallados.Add(selectedExercise);
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al seleccionar ejercicio: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void RemoveEjercicio(Ejercicio ejercicio)
        {
            if (ejercicio != null)
            {
                Ejercicios.Remove(ejercicio._id);
                EjerciciosDetallados.Remove(ejercicio);
                ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
            }
        }

        private async Task LoadEjerciciosDetallados()
        {
            try
            {
                EjerciciosDetallados.Clear();

                // Cargar todos los ejercicios para poder buscar por ID
                var todosEjercicios = await _dataService.GetAllEjerciciosAsync();

                foreach (var ejercicioId in Ejercicios)
                {
                    var ejercicio = todosEjercicios.FirstOrDefault(e => e._id == ejercicioId);
                    if (ejercicio != null)
                    {
                        EjerciciosDetallados.Add(ejercicio);
                    }
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error cargando detalles de ejercicios: {ex.Message}");
            }
        }

        private void AddMusculo()
        {
            if (!string.IsNullOrWhiteSpace(MusculoTemp))
            {
                var musculo = MusculoTemp.Trim();
                if (!Musculo.Contains(musculo))
                {
                    Musculo.Add(musculo);
                    MusculoTemp = string.Empty;
                }
            }
        }

        private void RemoveMusculo(string musculo)
        {
            if (!string.IsNullOrEmpty(musculo) && Musculo.Contains(musculo))
            {
                Musculo.Remove(musculo);
            }
        }

        private async Task SaveTrainingAsync()
        {
            try
            {
                IsLoading = true;

                if (IsEditMode)
                {
                    await UpdateTrainingAsync();
                }
                else
                {
                    await CreateTrainingAsync();
                }

                MessageBox.Show(
                    IsEditMode ? "Entrenamiento actualizado exitosamente." : "Entrenamiento creado exitosamente.",
                    "Éxito",
                    MessageBoxButton.OK,
                    MessageBoxImage.Information);

                RequestClose?.Invoke(true);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    $"Error al {(IsEditMode ? "actualizar" : "crear")} entrenamiento: {ex.Message}",
                    "Error",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task CreateTrainingAsync()
        {
            var newTraining = new Entrenamiento
            {
                nombre = Nombre,
                categoria = Categoria,
                musculoPrincipal = MusculoPrincipal,
                duracion = Duracion,
                foto = Foto,
                musculo = Musculo.ToList(),
                likes = 0, // Siempre empezar con 0 likes
                ejercicios = Ejercicios.ToList(),
                creador = Creador, // Aquí ya está el ID del usuario actual
                aprobado = Aprobado,
                pedido = Pedido,
                motivoRechazo = MotivoRechazo
            };

            var result = await _dataService.CreateEntrenamientoAsync(newTraining);
            if (result == null)
            {
                throw new Exception("No se pudo crear el entrenamiento");
            }
        }

        private async Task UpdateTrainingAsync()
        {
            if (_originalTraining == null) return;

            // Crear entrenamiento con los datos actualizados
            var updatedTraining = new Entrenamiento
            {
                _id = Id,
                nombre = Nombre,
                categoria = Categoria,
                musculoPrincipal = MusculoPrincipal,
                duracion = Duracion,
                foto = Foto,
                musculo = Musculo.ToList(),
                likes = Likes, // Mantener los likes existentes
                ejercicios = Ejercicios.ToList(),
                creador = Creador, // Mantener el creador original
                aprobado = Aprobado,
                pedido = Pedido,
                motivoRechazo = MotivoRechazo
            };

            var success = await _dataService.UpdateEntrenamientoAsync(updatedTraining);
            if (!success)
            {
                throw new Exception("No se pudo actualizar el entrenamiento");
            }
        }

        private void SelectImage()
        {
            try
            {
                var openFileDialog = new Microsoft.Win32.OpenFileDialog
                {
                    Title = "Seleccionar Imagen del Entrenamiento",
                    Filter = "Archivos de Imagen|*.jpg;*.jpeg;*.png;*.bmp;*.gif|" +
                             "JPEG|*.jpg;*.jpeg|" +
                             "PNG|*.png|" +
                             "BMP|*.bmp|" +
                             "GIF|*.gif|" +
                             "Todos los archivos|*.*",
                    FilterIndex = 1,
                    Multiselect = false
                };

                if (openFileDialog.ShowDialog() == true)
                {
                    string filePath = openFileDialog.FileName;

                    // Verificar el tamaño del archivo (máximo 5MB)
                    var fileInfo = new System.IO.FileInfo(filePath);
                    if (fileInfo.Length > 5 * 1024 * 1024) // 5MB
                    {
                        MessageBox.Show("La imagen es demasiado grande. Por favor, selecciona una imagen menor a 5MB.",
                            "Imagen muy grande", MessageBoxButton.OK, MessageBoxImage.Warning);
                        return;
                    }

                    // Convertir imagen a Base64
                    string base64Image = ConvertImageToBase64(filePath);
                    if (!string.IsNullOrEmpty(base64Image))
                    {
                        Foto = base64Image;
                        MessageBox.Show("Imagen cargada exitosamente.", "Éxito",
                            MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar la imagen: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        private void RemoveImage()
        {
            var result = MessageBox.Show("¿Estás seguro de que quieres eliminar la imagen del entrenamiento?",
                "Confirmar", MessageBoxButton.YesNo, MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                Foto = string.Empty;
                MessageBox.Show("Imagen eliminada.", "Información",
                    MessageBoxButton.OK, MessageBoxImage.Information);
            }
        }

        private string ConvertImageToBase64(string imagePath)
        {
            try
            {
                // Leer la imagen y redimensionarla si es necesario
                using (var originalImage = System.Drawing.Image.FromFile(imagePath))
                {
                    // Redimensionar si es muy grande (máximo 800x800)
                    var resizedImage = ResizeImage(originalImage, 800, 800);

                    using (var ms = new System.IO.MemoryStream())
                    {
                        // Guardar como JPEG con calidad del 85%
                        var jpegEncoder = GetEncoder(System.Drawing.Imaging.ImageFormat.Jpeg);
                        var encoderParams = new System.Drawing.Imaging.EncoderParameters(1);
                        encoderParams.Param[0] = new System.Drawing.Imaging.EncoderParameter(
                            System.Drawing.Imaging.Encoder.Quality, 85L);

                        resizedImage.Save(ms, jpegEncoder, encoderParams);
                        byte[] imageBytes = ms.ToArray();

                        // Crear el string Base64 con el prefijo data:image
                        return "data:image/jpeg;base64," + Convert.ToBase64String(imageBytes);
                    }
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error convirtiendo imagen: {ex.Message}");
                MessageBox.Show($"Error al procesar la imagen: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Error);
                return string.Empty;
            }
        }

        private System.Drawing.Image ResizeImage(System.Drawing.Image image, int maxWidth, int maxHeight)
        {
            var ratioX = (double)maxWidth / image.Width;
            var ratioY = (double)maxHeight / image.Height;
            var ratio = Math.Min(ratioX, ratioY);

            var newWidth = (int)(image.Width * ratio);
            var newHeight = (int)(image.Height * ratio);

            var newImage = new System.Drawing.Bitmap(newWidth, newHeight);
            using (var graphics = System.Drawing.Graphics.FromImage(newImage))
            {
                graphics.CompositingQuality = System.Drawing.Drawing2D.CompositingQuality.HighQuality;
                graphics.InterpolationMode = System.Drawing.Drawing2D.InterpolationMode.HighQualityBicubic;
                graphics.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.HighQuality;
                graphics.DrawImage(image, 0, 0, newWidth, newHeight);
            }

            return newImage;
        }

        private System.Drawing.Imaging.ImageCodecInfo GetEncoder(System.Drawing.Imaging.ImageFormat format)
        {
            var codecs = System.Drawing.Imaging.ImageCodecInfo.GetImageDecoders();
            foreach (var codec in codecs)
            {
                if (codec.FormatID == format.Guid)
                {
                    return codec;
                }
            }
            return null;
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