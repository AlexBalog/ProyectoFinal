using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using System.Windows.Media;
using ProyectoFinal.Models;
using ProyectoFinal.Services;
using ProyectoFinal.Converters;

namespace ProyectoFinal.Views
{
    public partial class TrainingRequestDetailWindow : Window
    {
        private readonly Entrenamiento _entrenamiento;
        private readonly IDataService _dataService;
        private bool _actionTaken = false;
        private readonly Base64ToImageConverter _imageConverter;

        public TrainingRequestDetailWindow(Entrenamiento entrenamiento)
        {
            InitializeComponent();
            _entrenamiento = entrenamiento ?? throw new ArgumentNullException(nameof(entrenamiento));
            _dataService = new DataService(new ApiService());
            _imageConverter = new Base64ToImageConverter();

            // Establecer el DataContext para el binding
            this.DataContext = _entrenamiento;

            // Cargar ejercicios de forma asíncrona
            _ = LoadExercisesAsync();
        }

        private async Task LoadExercisesAsync()
        {
            try
            {
                if (_entrenamiento.ejercicios != null && _entrenamiento.ejercicios.Count > 0)
                {
                    // Cargar todos los ejercicios disponibles
                    var todosEjercicios = await _dataService.GetAllEjerciciosAsync();
                    var ejerciciosEncontrados = new List<Ejercicio>();

                    // Filtrar solo los ejercicios que están en el entrenamiento
                    foreach (var ejercicioId in _entrenamiento.ejercicios)
                    {
                        var ejercicio = todosEjercicios.FirstOrDefault(e => e._id == ejercicioId);
                        if (ejercicio != null)
                        {
                            ejerciciosEncontrados.Add(ejercicio);
                        }
                        else
                        {
                            System.Diagnostics.Debug.WriteLine($"Ejercicio no encontrado: {ejercicioId}");
                        }
                    }

                    // Actualizar la UI en el hilo principal
                    Dispatcher.Invoke(() => {
                        EjerciciosPanel.Children.Clear();

                        if (ejerciciosEncontrados.Count > 0)
                        {
                            foreach (var ejercicio in ejerciciosEncontrados)
                            {
                                var ejercicioUI = CreateExerciseUIElement(ejercicio);
                                EjerciciosPanel.Children.Add(ejercicioUI);
                            }
                        }
                        else
                        {
                            ShowNoExercisesMessage();
                        }

                        // Mostrar ejercicios no encontrados si los hay
                        var noEncontrados = _entrenamiento.ejercicios.Count - ejerciciosEncontrados.Count;
                        if (noEncontrados > 0)
                        {
                            var warningElement = CreateWarningElement($"⚠️ {noEncontrados} ejercicio(s) no pudieron cargarse");
                            EjerciciosPanel.Children.Add(warningElement);
                        }
                    });
                }
                else
                {
                    Dispatcher.Invoke(() => {
                        EjerciciosPanel.Children.Clear();
                        ShowNoExercisesMessage();
                    });
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error al cargar ejercicios: {ex.Message}");
                Dispatcher.Invoke(() => {
                    EjerciciosPanel.Children.Clear();
                    ShowErrorMessage($"Error al cargar ejercicios: {ex.Message}");
                });
            }
        }

        private Border CreateExerciseUIElement(Ejercicio ejercicio)
        {
            // Border principal del ejercicio
            var exerciseBorder = new Border
            {
                Background = FindResource("AccentBrush") as Brush,
                BorderBrush = FindResource("PrimaryBrush") as Brush,
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(8),
                Padding = new Thickness(15),
                Margin = new Thickness(0, 0, 0, 10)
            };

            // Grid principal
            var mainGrid = new Grid();
            mainGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = GridLength.Auto }); // Imagen
            mainGrid.ColumnDefinitions.Add(new ColumnDefinition { Width = new GridLength(1, GridUnitType.Star) }); // Contenido

            // === IMAGEN ===
            var imageBorder = new Border
            {
                Width = 60,
                Height = 60,
                Background = FindResource("SurfaceBrush") as Brush,
                CornerRadius = new CornerRadius(8),
                Margin = new Thickness(0, 0, 15, 0)
            };

            var imageGrid = new Grid();

            // Imagen del ejercicio
            var exerciseImage = new Image
            {
                Stretch = Stretch.UniformToFill
            };

            // Convertir imagen si existe
            try
            {
                if (!string.IsNullOrEmpty(ejercicio.foto))
                {
                    var imageSource = _imageConverter.Convert(ejercicio.foto, typeof(ImageSource), null, null) as ImageSource;
                    exerciseImage.Source = imageSource;
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error convirtiendo imagen de ejercicio {ejercicio.nombre}: {ex.Message}");
            }

            // Clip para imagen redondeada
            exerciseImage.Clip = new RectangleGeometry(new Rect(0, 0, 60, 60), 6, 6);

            // Placeholder cuando no hay imagen
            var placeholderText = new TextBlock
            {
                Text = "🏋️",
                FontSize = 24,
                HorizontalAlignment = HorizontalAlignment.Center,
                VerticalAlignment = VerticalAlignment.Center,
                Foreground = FindResource("TextBrushSecondary") as Brush
            };

            // Lógica de visibilidad del placeholder
            if (exerciseImage.Source == null)
            {
                placeholderText.Visibility = Visibility.Visible;
                exerciseImage.Visibility = Visibility.Collapsed;
            }
            else
            {
                placeholderText.Visibility = Visibility.Collapsed;
                exerciseImage.Visibility = Visibility.Visible;
            }

            imageGrid.Children.Add(exerciseImage);
            imageGrid.Children.Add(placeholderText);
            imageBorder.Child = imageGrid;
            Grid.SetColumn(imageBorder, 0);

            // === INFORMACIÓN ===
            var infoPanel = new StackPanel
            {
                VerticalAlignment = VerticalAlignment.Center
            };

            // Nombre del ejercicio
            var nameText = new TextBlock
            {
                Text = ejercicio.nombre,
                FontWeight = FontWeights.SemiBold,
                FontSize = 16,
                Foreground = FindResource("TextBrushPrimary") as Brush,
                Margin = new Thickness(0, 0, 0, 5),
                TextWrapping = TextWrapping.Wrap
            };

            // Músculo
            var muscleText = new TextBlock
            {
                Text = $"💪 {ejercicio.musculo}",
                FontSize = 13,
                Foreground = FindResource("PrimaryBrush") as Brush,
                Margin = new Thickness(0, 0, 0, 5)
            };

            // Descripción
            var descriptionText = new TextBlock
            {
                Text = ejercicio.descripcion,
                FontSize = 12,
                Foreground = FindResource("TextBrushSecondary") as Brush,
                TextWrapping = TextWrapping.Wrap,
                MaxHeight = 40,
                TextTrimming = TextTrimming.WordEllipsis
            };

            // Información adicional (consejos y tutorial)
            var additionalInfoPanel = new StackPanel
            {
                Orientation = Orientation.Horizontal,
                Margin = new Thickness(0, 8, 0, 0)
            };

            // Indicador de consejos
            if (ejercicio.consejos != null && ejercicio.consejos.Any(c => !string.IsNullOrWhiteSpace(c)))
            {
                var consejosIndicator = new Border
                {
                    Background = FindResource("SuccessBrush") as Brush,
                    CornerRadius = new CornerRadius(10),
                    Padding = new Thickness(8, 2, 0, 2),
                    Margin = new Thickness(0, 0, 5, 0)
                };

                var consejosText = new TextBlock
                {
                    Text = "💡 Consejos",
                    FontSize = 10,
                    Foreground = Brushes.White,
                    FontWeight = FontWeights.SemiBold
                };

                consejosIndicator.Child = consejosText;
                additionalInfoPanel.Children.Add(consejosIndicator);
            }

            // Indicador de tutorial
            if (!string.IsNullOrWhiteSpace(ejercicio.tutorial))
            {
                var tutorialIndicator = new Border
                {
                    Background = FindResource("WarningBrush") as Brush,
                    CornerRadius = new CornerRadius(10),
                    Padding = new Thickness(8, 2, 0, 2),
                    Margin = new Thickness(0, 0, 5, 0)
                };

                var tutorialText = new TextBlock
                {
                    Text = "🎥 Tutorial",
                    FontSize = 10,
                    Foreground = Brushes.White,
                    FontWeight = FontWeights.SemiBold
                };

                tutorialIndicator.Child = tutorialText;
                additionalInfoPanel.Children.Add(tutorialIndicator);
            }

            // Agregar elementos al panel de información
            infoPanel.Children.Add(nameText);
            infoPanel.Children.Add(muscleText);
            infoPanel.Children.Add(descriptionText);
            if (additionalInfoPanel.Children.Count > 0)
            {
                infoPanel.Children.Add(additionalInfoPanel);
            }

            Grid.SetColumn(infoPanel, 1);

            // Agregar elementos al grid principal
            mainGrid.Children.Add(imageBorder);
            mainGrid.Children.Add(infoPanel);

            // Establecer el grid como contenido del border
            exerciseBorder.Child = mainGrid;

            return exerciseBorder;
        }

        private void ShowNoExercisesMessage()
        {
            var noExercisesElement = new Border
            {
                Background = FindResource("SurfaceBrush") as Brush,
                BorderBrush = FindResource("AccentBrush") as Brush,
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(8),
                Padding = new Thickness(20),
                Margin = new Thickness(0, 0, 0, 10)
            };

            var noExercisesPanel = new StackPanel
            {
                HorizontalAlignment = HorizontalAlignment.Center
            };

            var iconText = new TextBlock
            {
                Text = "🤷‍♂️",
                FontSize = 32,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 0, 10)
            };

            var messageText = new TextBlock
            {
                Text = "No se especificaron ejercicios para este entrenamiento",
                Foreground = FindResource("TextBrushSecondary") as Brush,
                FontStyle = FontStyles.Italic,
                HorizontalAlignment = HorizontalAlignment.Center
            };

            noExercisesPanel.Children.Add(iconText);
            noExercisesPanel.Children.Add(messageText);
            noExercisesElement.Child = noExercisesPanel;

            EjerciciosPanel.Children.Add(noExercisesElement);
        }

        private void ShowErrorMessage(string errorMessage)
        {
            var errorElement = new Border
            {
                Background = new SolidColorBrush(Color.FromRgb(45, 27, 27)),
                BorderBrush = new SolidColorBrush(Color.FromRgb(244, 67, 54)),
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(8),
                Padding = new Thickness(20),
                Margin = new Thickness(0, 0, 0, 10)
            };

            var errorPanel = new StackPanel
            {
                HorizontalAlignment = HorizontalAlignment.Center
            };

            var iconText = new TextBlock
            {
                Text = "❌",
                FontSize = 24,
                HorizontalAlignment = HorizontalAlignment.Center,
                Margin = new Thickness(0, 0, 0, 10)
            };

            var messageText = new TextBlock
            {
                Text = errorMessage,
                Foreground = new SolidColorBrush(Color.FromRgb(244, 67, 54)),
                HorizontalAlignment = HorizontalAlignment.Center,
                TextWrapping = TextWrapping.Wrap
            };

            errorPanel.Children.Add(iconText);
            errorPanel.Children.Add(messageText);
            errorElement.Child = errorPanel;

            EjerciciosPanel.Children.Add(errorElement);
        }

        private Border CreateWarningElement(string warningMessage)
        {
            var warningElement = new Border
            {
                Background = new SolidColorBrush(Color.FromRgb(45, 35, 27)),
                BorderBrush = FindResource("WarningBrush") as Brush,
                BorderThickness = new Thickness(1),
                CornerRadius = new CornerRadius(8),
                Padding = new Thickness(15),
                Margin = new Thickness(0, 10, 0, 0)
            };

            var warningText = new TextBlock
            {
                Text = warningMessage,
                Foreground = FindResource("WarningBrush") as Brush,
                FontSize = 12,
                HorizontalAlignment = HorizontalAlignment.Center,
                TextWrapping = TextWrapping.Wrap
            };

            warningElement.Child = warningText;
            return warningElement;
        }

        #region Eventos de ventana (sin cambios)
        private void Header_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        private void BtnClose_Click(object sender, RoutedEventArgs e)
        {
            this.DialogResult = _actionTaken;
            this.Close();
        }

        private void BtnCancel_Click(object sender, RoutedEventArgs e)
        {
            this.DialogResult = _actionTaken;
            this.Close();
        }

        private async void BtnApprove_Click(object sender, RoutedEventArgs e)
        {
            var result = MessageBox.Show(
                $"¿Estás seguro de que deseas aprobar el entrenamiento '{_entrenamiento.nombre}'?\n\n" +
                "Una vez aprobado, estará disponible para todos los usuarios.",
                "Confirmar Aprobación",
                MessageBoxButton.YesNo,
                MessageBoxImage.Question);

            if (result == MessageBoxResult.Yes)
            {
                await ProcessApproval();
            }
        }

        private async void BtnReject_Click(object sender, RoutedEventArgs e)
        {
            var motivoWindow = new MotivRejection();
            motivoWindow.Owner = this;

            var result = motivoWindow.ShowDialog();

            if (result == true && !string.IsNullOrWhiteSpace(motivoWindow.MotivoRechazo))
            {
                await ProcessRejection(motivoWindow.MotivoRechazo);
            }
        }

        private async Task ProcessApproval()
        {
            try
            {
                ShowLoading(true);

                _entrenamiento.aprobado = true;
                _entrenamiento.pedido = true;
                _entrenamiento.motivoRechazo = null;

                var success = await _dataService.UpdateEntrenamientoAsync(_entrenamiento);

                if (success)
                {
                    _actionTaken = true;
                    MessageBox.Show("¡Nuevo entrenamiento aprobado!", "Éxito",
                        MessageBoxButton.OK, MessageBoxImage.Information);

                    this.DialogResult = true;
                    this.Close();
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
                ShowLoading(false);
            }
        }

        private async Task ProcessRejection(string motivo)
        {
            try
            {
                ShowLoading(true);

                _entrenamiento.aprobado = false;
                _entrenamiento.pedido = false;
                _entrenamiento.motivoRechazo = motivo;

                var success = await _dataService.UpdateEntrenamientoAsync(_entrenamiento);

                if (success)
                {
                    _actionTaken = true;
                    MessageBox.Show("Entrenamiento rechazado correctamente.", "Información",
                        MessageBoxButton.OK, MessageBoxImage.Information);

                    this.DialogResult = true;
                    this.Close();
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
                ShowLoading(false);
            }
        }

        private void ShowLoading(bool show)
        {
            LoadingOverlay.Visibility = show ? Visibility.Visible : Visibility.Collapsed;
            btnApprove.IsEnabled = !show;
            btnReject.IsEnabled = !show;
            btnCancel.IsEnabled = !show;
        }
        #endregion
    }

    // Ventana simple para pedir motivo de rechazo (sin cambios)
    public partial class MotivRejection : Window
    {
        public string MotivoRechazo { get; private set; }

        public MotivRejection()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            this.Title = "Motivo de Rechazo";
            this.Width = 500;
            this.Height = 300;
            this.WindowStyle = WindowStyle.None;
            this.WindowStartupLocation = WindowStartupLocation.CenterOwner;
            this.Background = System.Windows.Media.Brushes.Transparent;
            this.AllowsTransparency = true;

            var mainBorder = new Border
            {
                Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(18, 18, 18)),
                CornerRadius = new CornerRadius(15),
                Padding = new Thickness(30)
            };

            var stackPanel = new StackPanel();

            var title = new TextBlock
            {
                Text = "Motivo de Rechazo",
                FontSize = 18,
                FontWeight = FontWeights.Bold,
                Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(138, 43, 226)),
                Margin = new Thickness(0, 0, 0, 20),
                HorizontalAlignment = HorizontalAlignment.Center
            };

            var instruction = new TextBlock
            {
                Text = "Por favor, especifica el motivo por el cual se rechaza este entrenamiento:",
                Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(224, 224, 224)),
                Margin = new Thickness(0, 0, 0, 15),
                TextWrapping = TextWrapping.Wrap
            };

            var motivoTextBox = new TextBox
            {
                Name = "MotivoTextBox",
                Height = 100,
                Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(30, 30, 30)),
                Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(255, 255, 255)),
                BorderBrush = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(51, 51, 51)),
                BorderThickness = new Thickness(1),
                Padding = new Thickness(10),
                TextWrapping = TextWrapping.Wrap,
                AcceptsReturn = true,
                VerticalScrollBarVisibility = ScrollBarVisibility.Auto,
                Margin = new Thickness(0, 0, 0, 20)
            };

            var buttonPanel = new StackPanel
            {
                Orientation = Orientation.Horizontal,
                HorizontalAlignment = HorizontalAlignment.Right
            };

            var cancelButton = new Button
            {
                Content = "Cancelar",
                Width = 100,
                Height = 35,
                Margin = new Thickness(0, 0, 10, 0),
                Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(51, 51, 51)),
                Foreground = System.Windows.Media.Brushes.White,
                BorderThickness = new Thickness(0),
                Cursor = Cursors.Hand
            };

            var acceptButton = new Button
            {
                Content = "Rechazar",
                Width = 100,
                Height = 35,
                Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(255, 152, 0)),
                Foreground = System.Windows.Media.Brushes.White,
                BorderThickness = new Thickness(0),
                Cursor = Cursors.Hand
            };

            cancelButton.Click += (s, e) =>
            {
                this.DialogResult = false;
                this.Close();
            };

            acceptButton.Click += (s, e) =>
            {
                var motivo = motivoTextBox.Text?.Trim();
                if (string.IsNullOrEmpty(motivo))
                {
                    MessageBox.Show("Por favor, ingrese un motivo para el rechazo.", "Campo requerido",
                        MessageBoxButton.OK, MessageBoxImage.Warning);
                    return;
                }

                this.MotivoRechazo = motivo;
                this.DialogResult = true;
                this.Close();
            };

            buttonPanel.Children.Add(cancelButton);
            buttonPanel.Children.Add(acceptButton);

            stackPanel.Children.Add(title);
            stackPanel.Children.Add(instruction);
            stackPanel.Children.Add(motivoTextBox);
            stackPanel.Children.Add(buttonPanel);

            mainBorder.Child = stackPanel;
            this.Content = mainBorder;

            mainBorder.MouseLeftButtonDown += (s, e) =>
            {
                if (e.ChangedButton == MouseButton.Left)
                    this.DragMove();
            };

            this.Loaded += (s, e) => motivoTextBox.Focus();
        }
    }
}