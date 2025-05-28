using System;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;
using ProyectoFinal.Models;
using ProyectoFinal.Services;

namespace ProyectoFinal.Views
{
    public partial class TrainingRequestDetailWindow : Window
    {
        private readonly Entrenamiento _entrenamiento;
        private readonly IDataService _dataService;
        private bool _actionTaken = false;

        public TrainingRequestDetailWindow(Entrenamiento entrenamiento)
        {
            InitializeComponent();
            _entrenamiento = entrenamiento ?? throw new ArgumentNullException(nameof(entrenamiento));
            _dataService = new DataService(new ApiService());

            // Establecer el DataContext para el binding
            this.DataContext = _entrenamiento;

            // Cargar ejercicios
            LoadExercises();
        }

        private async void LoadExercises()
        {
            try
            {
                if (_entrenamiento.ejercicios != null && _entrenamiento.ejercicios.Count > 0)
                {
                    foreach (var ejercicioId in _entrenamiento.ejercicios)
                    {
                        try
                        {
                            // Aquí podrías cargar los detalles de cada ejercicio si tienes un método para ello
                            // Por ahora solo mostramos los IDs
                            var exercisePanel = new StackPanel
                            {
                                Orientation = Orientation.Horizontal,
                                Margin = new Thickness(0, 2, 0, 2)
                            };

                            var bullet = new TextBlock
                            {
                                Text = "• ",
                                Foreground = this.FindResource("PrimaryBrush") as System.Windows.Media.Brush,
                                FontWeight = FontWeights.Bold
                            };

                            var exerciseText = new TextBlock
                            {
                                Text = $"Ejercicio ID: {ejercicioId}",
                                Foreground = this.FindResource("TextBrushPrimary") as System.Windows.Media.Brush
                            };

                            exercisePanel.Children.Add(bullet);
                            exercisePanel.Children.Add(exerciseText);
                            EjerciciosPanel.Children.Add(exercisePanel);
                        }
                        catch (Exception ex)
                        {
                            System.Diagnostics.Debug.WriteLine($"Error al cargar ejercicio {ejercicioId}: {ex.Message}");
                        }
                    }
                }
                else
                {
                    var noExercises = new TextBlock
                    {
                        Text = "No se especificaron ejercicios para este entrenamiento.",
                        Foreground = this.FindResource("TextBrushSecondary") as System.Windows.Media.Brush,
                        FontStyle = FontStyles.Italic
                    };
                    EjerciciosPanel.Children.Add(noExercises);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Error al cargar ejercicios: {ex.Message}", "Error",
                    MessageBoxButton.OK, MessageBoxImage.Warning);
            }
        }

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
            // Crear ventana para pedir motivo de rechazo
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

                // Actualizar el entrenamiento
                _entrenamiento.aprobado = true;
                _entrenamiento.pedido = true;
                _entrenamiento.motivoRechazo = null; // Limpiar motivo de rechazo si lo había

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

                // Actualizar el entrenamiento
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
    }

    // Ventana simple para pedir motivo de rechazo
    public partial class MotivRejection : Window
    {
        public string MotivoRechazo { get; private set; }

        public MotivRejection()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            // Configuración básica de la ventana
            this.Title = "Motivo de Rechazo";
            this.Width = 500;
            this.Height = 300;
            this.WindowStyle = WindowStyle.None;
            this.WindowStartupLocation = WindowStartupLocation.CenterOwner;
            this.Background = System.Windows.Media.Brushes.Transparent;
            this.AllowsTransparency = true;

            // Crear el contenido
            var mainBorder = new Border
            {
                Background = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(18, 18, 18)),
                CornerRadius = new CornerRadius(15),
                Padding = new Thickness(30)
            };

            var stackPanel = new StackPanel();

            // Título
            var title = new TextBlock
            {
                Text = "Motivo de Rechazo",
                FontSize = 18,
                FontWeight = FontWeights.Bold,
                Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(138, 43, 226)),
                Margin = new Thickness(0, 0, 0, 20),
                HorizontalAlignment = HorizontalAlignment.Center
            };

            // Instrucción
            var instruction = new TextBlock
            {
                Text = "Por favor, especifica el motivo por el cual se rechaza este entrenamiento:",
                Foreground = new System.Windows.Media.SolidColorBrush(System.Windows.Media.Color.FromRgb(224, 224, 224)),
                Margin = new Thickness(0, 0, 0, 15),
                TextWrapping = TextWrapping.Wrap
            };

            // TextBox para el motivo
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

            // Panel de botones
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

            // Eventos
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

            // Agregar elementos
            buttonPanel.Children.Add(cancelButton);
            buttonPanel.Children.Add(acceptButton);

            stackPanel.Children.Add(title);
            stackPanel.Children.Add(instruction);
            stackPanel.Children.Add(motivoTextBox);
            stackPanel.Children.Add(buttonPanel);

            mainBorder.Child = stackPanel;
            this.Content = mainBorder;

            // Permitir mover la ventana
            mainBorder.MouseLeftButtonDown += (s, e) =>
            {
                if (e.ChangedButton == MouseButton.Left)
                    this.DragMove();
            };

            // Enfocar el TextBox al cargar
            this.Loaded += (s, e) => motivoTextBox.Focus();
        }
    }
}