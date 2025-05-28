using System;
using System.Globalization;
using System.IO;
using System.Windows.Data;
using System.Windows.Media.Imaging;

namespace ProyectoFinal.Converters
{
    public class Base64ToImageConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
        {
            try
            {
                if (value is string base64String && !string.IsNullOrEmpty(base64String))
                {
                    // Verificar si ya tiene el prefijo data:image
                    if (base64String.StartsWith("data:image"))
                    {
                        // Extraer solo la parte base64
                        var base64Data = base64String.Substring(base64String.IndexOf(',') + 1);
                        base64String = base64Data;
                    }

                    // Convertir base64 a bytes
                    byte[] imageBytes = System.Convert.FromBase64String(base64String);

                    // Crear BitmapImage
                    var bitmap = new BitmapImage();
                    using (var stream = new MemoryStream(imageBytes))
                    {
                        bitmap.BeginInit();
                        bitmap.CacheOption = BitmapCacheOption.OnLoad;
                        bitmap.StreamSource = stream;
                        bitmap.EndInit();
                        bitmap.Freeze(); // Para poder usar en otros threads
                    }

                    return bitmap;
                }
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error al convertir base64 a imagen: {ex.Message}");
            }

            // Retornar imagen por defecto si hay error
            return CreateDefaultImage();
        }

        public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
        {
            throw new NotImplementedException();
        }

        private BitmapImage CreateDefaultImage()
        {
            try
            {
                // Crear una imagen por defecto simple (gris)
                var bitmap = new BitmapImage();
                bitmap.BeginInit();
                bitmap.UriSource = new Uri("pack://application:,,,/Images/default-training.png", UriKind.Absolute);
                bitmap.EndInit();
                return bitmap;
            }
            catch
            {
                // Si no hay imagen por defecto, retornar null
                return null;
            }
        }
    }
}