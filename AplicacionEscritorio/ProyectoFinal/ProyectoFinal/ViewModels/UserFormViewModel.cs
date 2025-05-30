using System;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Input;
using System.Text.RegularExpressions;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Utilities;

namespace ProyectoFinal.ViewModels
{
    public class UserFormViewModel : INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private Usuario? _originalUser;
        private bool _isEditMode;
        private bool _isLoading;
        private string _windowTitle;

        // Campos del usuario
        private string _id;
        private string _nombre;
        private string _apellido;
        private string _email;
        private string _contrasena;
        private string _confirmarContrasena;
        private DateTime? _fechaNacimiento;
        private string _sexo;
        private string _plan;
        private string _foto;
        private double? _altura;
        private double? _peso;
        private double? _imc;
        private string _nivelActividad;
        private double? _caloriasMantenimiento;
        private double? _objetivoCalorias;
        private double? _objetivoPeso;
        private int? _objetivoTiempo;
        private bool _formulario;

        // Propiedades de validación
        private string _emailError;

        public UserFormViewModel()
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

        public string Apellido
        {
            get => _apellido ?? string.Empty;
            set
            {
                if (_apellido != value)
                {
                    _apellido = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string Email
        {
            get => _email ?? string.Empty;
            set
            {
                if (_email != value)
                {
                    _email = value ?? string.Empty;
                    ValidateEmail();
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string EmailError
        {
            get => _emailError ?? string.Empty;
            set
            {
                if (_emailError != value)
                {
                    _emailError = value ?? string.Empty;
                    OnPropertyChanged();
                }
            }
        }

        public bool HasEmailError => !string.IsNullOrEmpty(EmailError);

        public string Contrasena
        {
            get => _contrasena ?? string.Empty;
            set
            {
                if (_contrasena != value)
                {
                    _contrasena = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public string ConfirmarContrasena
        {
            get => _confirmarContrasena ?? string.Empty;
            set
            {
                if (_confirmarContrasena != value)
                {
                    _confirmarContrasena = value ?? string.Empty;
                    OnPropertyChanged();
                    ((RelayCommand)SaveCommand).RaiseCanExecuteChanged();
                }
            }
        }

        public DateTime? FechaNacimiento
        {
            get => _fechaNacimiento;
            set
            {
                if (_fechaNacimiento != value)
                {
                    _fechaNacimiento = value;
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public string Sexo
        {
            get => _sexo ?? "Masculino";
            set
            {
                if (_sexo != value)
                {
                    _sexo = value ?? "Masculino";
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public string Plan
        {
            get => _plan ?? "Gratuito";
            set
            {
                if (_plan != value)
                {
                    _plan = value ?? "Gratuito";
                    OnPropertyChanged();
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
                }
            }
        }

        public double? Altura
        {
            get => _altura;
            set
            {
                if (_altura != value)
                {
                    _altura = value;
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public double? Peso
        {
            get => _peso;
            set
            {
                if (_peso != value)
                {
                    _peso = value;
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public double? IMC
        {
            get => _imc;
            set
            {
                if (_imc != value)
                {
                    _imc = value;
                    OnPropertyChanged();
                }
            }
        }

        public string NivelActividad
        {
            get => _nivelActividad ?? "Sedentario";
            set
            {
                if (_nivelActividad != value)
                {
                    _nivelActividad = value ?? "Sedentario";
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public double? CaloriasMantenimiento
        {
            get => _caloriasMantenimiento;
            set
            {
                if (_caloriasMantenimiento != value)
                {
                    _caloriasMantenimiento = value;
                    OnPropertyChanged();
                }
            }
        }

        public double? ObjetivoCalorias
        {
            get => _objetivoCalorias;
            set
            {
                if (_objetivoCalorias != value)
                {
                    _objetivoCalorias = value;
                    OnPropertyChanged();
                }
            }
        }

        public double? ObjetivoPeso
        {
            get => _objetivoPeso;
            set
            {
                if (_objetivoPeso != value)
                {
                    _objetivoPeso = value;
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public int? ObjetivoTiempo
        {
            get => _objetivoTiempo;
            set
            {
                if (_objetivoTiempo != value)
                {
                    _objetivoTiempo = value;
                    OnPropertyChanged();
                    CalculateAllMetrics();
                }
            }
        }

        public bool Formulario
        {
            get => _formulario;
            set
            {
                if (_formulario != value)
                {
                    _formulario = value;
                    OnPropertyChanged();
                }
            }
        }

        // Propiedades de solo lectura para mostrar/ocultar campos de contraseña
        public bool ShowPasswordFields => !IsEditMode;
        public bool ShowPasswordChangeOption => IsEditMode;
        #endregion

        #region Comandos
        public ICommand SaveCommand { get; private set; } = null!;
        public ICommand CancelCommand { get; private set; } = null!;
        public ICommand SelectImageCommand { get; private set; } = null!;
        public ICommand RemoveImageCommand { get; private set; } = null!;

        public bool HasImage => !string.IsNullOrEmpty(Foto);

        private void InitializeCommands()
        {
            SaveCommand = new RelayCommand(async () => await SaveUserAsync(), CanSave);
            CancelCommand = new RelayCommand(() => RequestClose?.Invoke(false));
            SelectImageCommand = new RelayCommand(() => SelectImage());
            RemoveImageCommand = new RelayCommand(() => RemoveImage());
        }

        private bool CanSave()
        {
            if (IsLoading) return false;

            // Validaciones básicas
            if (string.IsNullOrWhiteSpace(Nombre) ||
                string.IsNullOrWhiteSpace(Apellido) ||
                string.IsNullOrWhiteSpace(Email) ||
                HasEmailError)
            {
                return false;
            }

            // Validar contraseña solo en modo creación
            if (!IsEditMode)
            {
                if (string.IsNullOrWhiteSpace(Contrasena) ||
                    Contrasena.Length < 6 ||
                    Contrasena != ConfirmarContrasena)
                {
                    return false;
                }
            }

            return true;
        }
        #endregion

        #region Métodos públicos
        public void LoadUser(Usuario user)
        {
            if (user == null) return;

            _originalUser = user;
            IsEditMode = true;

            // Cargar datos del usuario
            Id = user._id;
            Nombre = user.nombre;
            Apellido = user.apellido;
            Email = user.email;
            FechaNacimiento = user.fechaNacimiento;
            Sexo = user.sexo ?? "Masculino";
            Plan = user.plan ?? "Gratuito";
            Foto = user.foto;
            Altura = user.altura;
            Peso = user.peso;
            IMC = user.IMC;
            NivelActividad = user.nivelActividad ?? "Sedentario";
            CaloriasMantenimiento = user.caloriasMantenimiento;
            ObjetivoCalorias = user.objetivoCalorias;
            ObjetivoPeso = user.objetivoPeso;
            ObjetivoTiempo = user.objetivoTiempo;
            Formulario = user.formulario;

            // Notificar cambios en propiedades dependientes
            OnPropertyChanged(nameof(ShowPasswordFields));
            OnPropertyChanged(nameof(ShowPasswordChangeOption));
        }

        public void SetCreateMode()
        {
            IsEditMode = false;
            _originalUser = null;
            InitializeDefaults();

            OnPropertyChanged(nameof(ShowPasswordFields));
            OnPropertyChanged(nameof(ShowPasswordChangeOption));
        }
        #endregion

        #region Métodos privados
        private void InitializeDefaults()
        {
            Id = string.Empty;
            Nombre = string.Empty;
            Apellido = string.Empty;
            Email = string.Empty;
            EmailError = string.Empty;
            Contrasena = string.Empty;
            ConfirmarContrasena = string.Empty;
            FechaNacimiento = null;
            Sexo = "Masculino";
            Plan = "Gratuito";
            Foto = string.Empty;
            Altura = null;
            Peso = null;
            IMC = null;
            NivelActividad = "Sedentario";
            CaloriasMantenimiento = null;
            ObjetivoCalorias = null;
            ObjetivoPeso = null;
            ObjetivoTiempo = null;
            Formulario = false;

            UpdateWindowTitle();
        }

        private void UpdateWindowTitle()
        {
            WindowTitle = IsEditMode ? "Editar Usuario" : "Crear Usuario";
        }

        private void ValidateEmail()
        {
            if (string.IsNullOrWhiteSpace(Email))
            {
                EmailError = string.Empty;
                OnPropertyChanged(nameof(HasEmailError));
                return;
            }

            var emailRegex = new Regex(@"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$");
            if (!emailRegex.IsMatch(Email))
            {
                EmailError = "Formato de email inválido";
            }
            else
            {
                EmailError = string.Empty;
            }
            OnPropertyChanged(nameof(HasEmailError));
        }

        /// <summary>
        /// Calcula todos los métricas automáticamente cuando cambian los datos relevantes
        /// </summary>
        private void CalculateAllMetrics()
        {
            // Calcular IMC
            CalculateIMC();

            // Calcular calorías si tenemos todos los datos necesarios
            CalculateCalories();

            // Verificar si el formulario está completo
            CheckFormularioCompleto();
        }

        private void CalculateIMC()
        {
            if (Altura.HasValue && Peso.HasValue && Altura.Value > 0)
            {
                double alturaMetros = Altura.Value / 100; // Convertir cm a metros
                IMC = Math.Round(Peso.Value / (alturaMetros * alturaMetros), 2);
            }
            else
            {
                IMC = null;
            }
        }

        /// <summary>
        /// Calcula calorías de mantenimiento y objetivo basado en las fórmulas de Android
        /// </summary>
        private void CalculateCalories()
        {
            // Verificar que tenemos todos los datos necesarios
            if (!FechaNacimiento.HasValue ||
                string.IsNullOrEmpty(Sexo) ||
                !Altura.HasValue || Altura.Value <= 0 ||
                !Peso.HasValue || Peso.Value <= 0 ||
                string.IsNullOrEmpty(NivelActividad))
            {
                CaloriasMantenimiento = null;
                ObjetivoCalorias = null;
                return;
            }

            try
            {
                // Calcular edad
                var edad = CalcularEdad(FechaNacimiento.Value);

                // Calcular TMB (Tasa Metabólica Basal) usando fórmula Mifflin-St Jeor
                double tmb;
                switch (Sexo.ToLower())
                {
                    case "masculino":
                    case "hombre":
                        tmb = 10 * Peso.Value + 6.25 * Altura.Value - 5 * edad + 5;
                        break;
                    case "femenino":
                    case "mujer":
                        tmb = 10 * Peso.Value + 6.25 * Altura.Value - 5 * edad - 161;
                        break;
                    default:
                        tmb = 10 * Peso.Value + 6.25 * Altura.Value - 5 * edad - 78; // Valor neutro
                        break;
                }

                // Calcular calorías de mantenimiento según nivel de actividad
                double factorActividad;
                switch (NivelActividad.ToLower())
                {
                    case "sedentario":
                        factorActividad = 1.2;
                        break;
                    case "ligero":
                        factorActividad = 1.375;
                        break;
                    case "moderado":
                        factorActividad = 1.55;
                        break;
                    case "activo":
                        factorActividad = 1.725;
                        break;
                    case "muy activo":
                        factorActividad = 1.9;
                        break;
                    default:
                        factorActividad = 1.4; // Valor predeterminado moderado
                        break;
                }

                CaloriasMantenimiento = Math.Round(tmb * factorActividad, 0);

                // Calcular objetivo de calorías si tenemos peso objetivo y tiempo objetivo
                if (ObjetivoPeso.HasValue && ObjetivoPeso.Value > 0 &&
                    ObjetivoTiempo.HasValue && ObjetivoTiempo.Value > 0)
                {
                    // Diferencia de peso que se quiere alcanzar
                    double diferenciaPeso = ObjetivoPeso.Value - Peso.Value;

                    // Convertir semanas a días para el cálculo
                    double tiempoEnDias = ObjetivoTiempo.Value * 7;

                    // Calcular ajuste calórico diario (7700 kcal por kg de grasa)
                    double ajusteCaloricoSemanal = (7700 * diferenciaPeso) / (ObjetivoTiempo.Value * 7);

                    ObjetivoCalorias = Math.Round(CaloriasMantenimiento.Value + ajusteCaloricoSemanal, 0);
                }
                else
                {
                    ObjetivoCalorias = null;
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error calculando calorías: {ex.Message}");
                CaloriasMantenimiento = null;
                ObjetivoCalorias = null;
            }
        }

        /// <summary>
        /// Calcula la edad a partir de una fecha de nacimiento
        /// </summary>
        private int CalcularEdad(DateTime fechaNacimiento)
        {
            var hoy = DateTime.Today;
            var edad = hoy.Year - fechaNacimiento.Year;

            // Ajustar si aún no ha cumplido años este año
            if (fechaNacimiento.Date > hoy.AddYears(-edad))
                edad--;

            return edad;
        }

        /// <summary>
        /// Verifica si el formulario está completo para marcar la propiedad Formulario como true
        /// </summary>
        private void CheckFormularioCompleto()
        {
            bool formularioCompleto = FechaNacimiento.HasValue &&
                                     !string.IsNullOrEmpty(Sexo) &&
                                     Altura.HasValue && Altura.Value > 0 &&
                                     Peso.HasValue && Peso.Value > 0 &&
                                     !string.IsNullOrEmpty(NivelActividad) &&
                                     ObjetivoPeso.HasValue && ObjetivoPeso.Value > 0 &&
                                     ObjetivoTiempo.HasValue && ObjetivoTiempo.Value > 0;

            Formulario = formularioCompleto;
        }

        private async Task SaveUserAsync()
        {
            try
            {
                IsLoading = true;

                // Calcular métricas finales antes de guardar
                CalculateAllMetrics();

                if (IsEditMode)
                {
                    await UpdateUserAsync();
                }
                else
                {
                    await CreateUserAsync();
                }

                MessageBox.Show(
                    IsEditMode ? "Usuario actualizado exitosamente." : "Usuario creado exitosamente.",
                    "Éxito",
                    MessageBoxButton.OK,
                    MessageBoxImage.Information);

                RequestClose?.Invoke(true);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    $"Error al {(IsEditMode ? "actualizar" : "crear")} usuario: {ex.Message}",
                    "Error",
                    MessageBoxButton.OK,
                    MessageBoxImage.Error);
            }
            finally
            {
                IsLoading = false;
            }
        }

        private async Task CreateUserAsync()
        {
            var newUser = new Usuario
            {
                nombre = Nombre,
                apellido = Apellido,
                email = Email,
                contrasena = Contrasena, // Se cifrará en el servidor
                fechaNacimiento = FechaNacimiento,
                sexo = Sexo,
                plan = Plan,
                foto = Foto,
                altura = Altura,
                peso = Peso,
                IMC = IMC,
                nivelActividad = NivelActividad,
                caloriasMantenimiento = CaloriasMantenimiento,
                objetivoCalorias = ObjetivoCalorias,
                objetivoPeso = ObjetivoPeso,
                objetivoTiempo = ObjetivoTiempo,
                formulario = Formulario
            };

            var result = await _dataService.CreateUsuarioAsync(newUser);
            if (result == null)
            {
                throw new Exception("No se pudo crear el usuario");
            }
        }

        private async Task UpdateUserAsync()
        {
            if (_originalUser == null) return;

            // Crear usuario con los datos actualizados
            var updatedUser = new Usuario
            {
                _id = Id,
                nombre = Nombre,
                apellido = Apellido,
                email = Email,
                fechaNacimiento = FechaNacimiento,
                sexo = Sexo,
                plan = Plan,
                foto = Foto,
                altura = Altura,
                peso = Peso,
                IMC = IMC,
                nivelActividad = NivelActividad,
                caloriasMantenimiento = CaloriasMantenimiento,
                objetivoCalorias = ObjetivoCalorias,
                objetivoPeso = ObjetivoPeso,
                objetivoTiempo = ObjetivoTiempo,
                formulario = Formulario
            };

            // Solo incluir contraseña si se especificó una nueva
            if (!string.IsNullOrWhiteSpace(Contrasena))
            {
                updatedUser.contrasena = Contrasena;
            }
            else
            {
                updatedUser.contrasena = _originalUser.contrasena; // Mantener la actual
            }

            var success = await _dataService.UpdateUsuarioAsync(updatedUser);
            if (!success)
            {
                throw new Exception("No se pudo actualizar el usuario");
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

        private void SelectImage()
        {
            try
            {
                var openFileDialog = new Microsoft.Win32.OpenFileDialog
                {
                    Title = "Seleccionar Imagen de Perfil",
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
                        OnPropertyChanged(nameof(HasImage));
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

        /// <summary>
        /// Remueve la imagen actual
        /// </summary>
        private void RemoveImage()
        {
            var result = MessageBox.Show("¿Estás seguro de que quieres eliminar la imagen de perfil?",
                "Confirmar", MessageBoxButton.YesNo, MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                Foto = string.Empty;
                OnPropertyChanged(nameof(HasImage));
                MessageBox.Show("Imagen eliminada.", "Información",
                    MessageBoxButton.OK, MessageBoxImage.Information);
            }
        }

        /// <summary>
        /// Convierte una imagen a Base64
        /// </summary>
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

                        // Crear el string Base64
                        return Convert.ToBase64String(imageBytes);
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

        /// <summary>
        /// Redimensiona una imagen manteniendo la proporción
        /// </summary>
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

        /// <summary>
        /// Obtiene el encoder para un formato de imagen específico
        /// </summary>
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
    }
}