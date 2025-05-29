using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
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
    public class ExerciseFormViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private Ejercicio? _originalExercise;
        private bool _isEditMode;
        private bool _isLoading;
        private string _windowTitle;

        // Campos del ejercicio
        private string _id;
        private string _nombre;
        private string _musculo;
        private string _descripcion;
        private string _foto;
        private string _tutorial;
        private ObservableCollection<string> _consejos;
        private string _nuevoConsejo;

        // Lista de músculos disponibles
        private readonly List<string> _musculosDisponibles = new List<string>
        {
            "Pecho", "Espalda", "Hombros", "Bíceps", "Tríceps",
            "Piernas", "Cuádriceps", "Isquiotibiales", "Glúteos",
            "Pantorrillas", "Abdominales", "Core", "Cardio"
        };

        public ExerciseFormViewModel()
        {
            _dataService = new DataService(new ApiService());
            _consejos = new ObservableCollection<string>();
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

        public string Musculo
        {
            get => _musculo ?? string.Empty;
            set
            {
                if (_musculo != value)
                {
                    _musculo = value ?? string.Empty;
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

        public string Tutorial
        {
            get => _tutorial ?? string.Empty;
            set
            {
                if (_tutorial != value)
                {
                    _tutorial = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public ObservableCollection<string> Consejos
        {
            get => _consejos;
            set
            {
                if (_consejos != value)
                {
                    _consejos = value ?? new ObservableCollection<string>();
                    OnPropertyChanged();
                }
            }
        }

        public string NuevoConsejo
        {
            get => _nuevoConsejo ?? string.Empty;
            set
            {
                if (_nuevoConsejo != value)
                {
                    _nuevoConsejo = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)AddConsejoCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public List<string> MusculosDisponibles => _musculosDisponibles;

        public bool HasImage => !string.IsNullOrEmpty(Foto);
        #endregion

        #region Comandos
        public ICommand SaveCommand { get; private set; } = null!;
        public ICommand CancelCommand { get; private set; } = null!;
        public ICommand SelectImageCommand { get; private set; } = null!;
        public ICommand RemoveImageCommand { get; private set; } = null!;
        public ICommand AddConsejoCommand { get; private set; } = null!;
        public ICommand RemoveConsejoCommand { get; private set; } = null!;

        private void InitializeCommands()
        {
            SaveCommand = new RelayCommand(async () => await SaveExerciseAsync(), CanSave);
            CancelCommand = new RelayCommand(() => RequestClose?.Invoke(false));
            SelectImageCommand = new RelayCommand(() => SelectImage());
            RemoveImageCommand = new RelayCommand(() => RemoveImage());
            AddConsejoCommand = new RelayCommand(() => AddConsejo(), CanAddConsejo);
            RemoveConsejoCommand = new RelayCommand<string>(consejo => RemoveConsejo(consejo));
        }

        private bool CanSave()
        {
            if (IsLoading) return false;

            return !string.IsNullOrWhiteSpace(Nombre) &&
                   !string.IsNullOrWhiteSpace(Musculo) &&
                   !string.IsNullOrWhiteSpace(Descripcion);
        }

        private bool CanAddConsejo()
        {
            return !string.IsNullOrWhiteSpace(NuevoConsejo);
        }
        #endregion

        #region Métodos públicos
        public void LoadExercise(Ejercicio exercise)
        {
            if (exercise == null) return;

            _originalExercise = exercise;
            IsEditMode = true;

            Id = exercise._id;
            Nombre = exercise.nombre;
            Musculo = exercise.musculo;
            Descripcion = exercise.descripcion;
            Foto = exercise.foto;
            Tutorial = exercise.tutorial;

            Consejos.Clear();
            if (exercise.consejos != null)
            {
                foreach (var consejo in exercise.consejos)
                {
                    if (!string.IsNullOrWhiteSpace(consejo))
                    {
                        Consejos.Add(consejo);
                    }
                }
            }
        }

        public void SetCreateMode()
        {
            IsEditMode = false;
            _originalExercise = null;
            InitializeDefaults();
        }
        #endregion

        #region Métodos privados
        private void InitializeDefaults()
        {
            Id = string.Empty;
            Nombre = string.Empty;
            Musculo = string.Empty;
            Descripcion = string.Empty;
            Foto = string.Empty;
            Tutorial = string.Empty;
            NuevoConsejo = string.Empty;

            Consejos.Clear();

            UpdateWindowTitle();
        }

        private void UpdateWindowTitle()
        {
            WindowTitle = IsEditMode ? "Editar Ejercicio" : "Crear Ejercicio";
        }

        private void AddConsejo()
        {
            if (!string.IsNullOrWhiteSpace(NuevoConsejo))
            {
                Consejos.Add(NuevoConsejo.Trim());
                NuevoConsejo = string.Empty;
            }
        }

        private void RemoveConsejo(string? consejo)
        {
            if (!string.IsNullOrEmpty(consejo) && Consejos.Contains(consejo))
            {
                Consejos.Remove(consejo);
            }
        }

        private async Task SaveExerciseAsync()
        {
            try
            {
                IsLoading = true;

                if (IsEditMode)
                {
                    await UpdateExerciseAsync();
                }
                else
                {
                    await CreateExerciseAsync();
                }

                MessageBox.Show(
                    IsEditMode ? "Ejercicio actualizado exitosamente." : "Ejercicio creado exitosamente.",
                    "Éxito",
                    MessageBoxButton.OK,
                    MessageBoxImage.Information);

                RequestClose?.Invoke(true);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    $"Error al {(IsEditMode ? "actualizar" : "crear")} ejercicio: {ex.Message}",
                    "Error",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task CreateExerciseAsync()
        {
            var newExercise = new Ejercicio
            {
                nombre = Nombre,
                musculo = Musculo,
                descripcion = Descripcion,
                foto = Foto,
                tutorial = Tutorial,
                consejos = new List<string>(Consejos)
            };

            var result = await _dataService.CreateEjercicioAsync(newExercise);
            if (result == null)
            {
                throw new Exception("No se pudo crear el ejercicio");
            }
        }

        private async Task UpdateExerciseAsync()
        {
            if (_originalExercise == null) return;

            var updatedExercise = new Ejercicio
            {
                _id = Id,
                nombre = Nombre,
                musculo = Musculo,
                descripcion = Descripcion,
                foto = Foto,
                tutorial = Tutorial,
                consejos = new List<string>(Consejos)
            };

            var success = await _dataService.UpdateEjercicioAsync(updatedExercise);
            if (!success)
            {
                throw new Exception("No se pudo actualizar el ejercicio");
            }
        }

        private void SelectImage()
        {
            try
            {
                var openFileDialog = new Microsoft.Win32.OpenFileDialog
                {
                    Title = "Seleccionar Imagen del Ejercicio",
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

                    var fileInfo = new System.IO.FileInfo(filePath);
                    if (fileInfo.Length > 5 * 1024 * 1024) // 5MB
                    {
                        MessageBox.Show("La imagen es demasiado grande. Por favor, selecciona una imagen menor a 5MB.",
                            "Imagen muy grande", MessageBoxButton.OK, MessageBoxImage.Warning);
                        return;
                    }

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
            var result = MessageBox.Show("¿Estás seguro de que quieres eliminar la imagen del ejercicio?",
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
                using (var originalImage = System.Drawing.Image.FromFile(imagePath))
                {
                    var resizedImage = ResizeImage(originalImage, 800, 800);

                    using (var ms = new System.IO.MemoryStream())
                    {
                        var jpegEncoder = GetEncoder(System.Drawing.Imaging.ImageFormat.Jpeg);
                        var encoderParams = new System.Drawing.Imaging.EncoderParameters(1);
                        encoderParams.Param[0] = new System.Drawing.Imaging.EncoderParameter(
                            System.Drawing.Imaging.Encoder.Quality, 85L);

                        resizedImage.Save(ms, jpegEncoder, encoderParams);
                        byte[] imageBytes = ms.ToArray();

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