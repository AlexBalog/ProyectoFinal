using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using Newtonsoft.Json;
using ProyectoFinal.Models;

namespace ProyectoFinal.Services
{
    public interface IUserService
    {
        void SaveToken(string token);
        string GetToken();
        void ClearToken();
        void SaveCredentials(UserCredentials credentials);
        UserCredentials GetSavedCredentials();
        void ClearSavedCredentials();
    }

    public class UserService : IUserService
    {
        private readonly string _tokenFileName = "token.dat";
        private readonly string _credentialsFileName = "credentials.dat";
        private readonly string _encryptionKey = "FitSphereSecretKey"; // Clave para encriptar datos

        // Métodos para el token
        public void SaveToken(string token)
        {
            if (string.IsNullOrEmpty(token))
                return;

            try
            {
                // Encriptar el token antes de guardarlo
                string encryptedToken = EncryptString(token);
                File.WriteAllText(_tokenFileName, encryptedToken);
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al guardar el token: {ex.Message}");
            }
        }

        public string GetToken()
        {
            try
            {
                if (File.Exists(_tokenFileName))
                {
                    string encryptedToken = File.ReadAllText(_tokenFileName);
                    return DecryptString(encryptedToken);
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al obtener el token: {ex.Message}");
            }

            return null;
        }

        public void ClearToken()
        {
            try
            {
                if (File.Exists(_tokenFileName))
                {
                    File.Delete(_tokenFileName);
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al eliminar el token: {ex.Message}");
            }
        }

        // Métodos para credenciales
        public void SaveCredentials(UserCredentials credentials)
        {
            if (credentials == null)
                return;

            try
            {
                // Serializar y encriptar las credenciales
                string json = JsonConvert.SerializeObject(credentials);
                string encryptedJson = EncryptString(json);
                File.WriteAllText(_credentialsFileName, encryptedJson);
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al guardar las credenciales: {ex.Message}");
            }
        }

        public UserCredentials GetSavedCredentials()
        {
            try
            {
                if (File.Exists(_credentialsFileName))
                {
                    string encryptedJson = File.ReadAllText(_credentialsFileName);
                    string json = DecryptString(encryptedJson);
                    return JsonConvert.DeserializeObject<UserCredentials>(json);
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al obtener las credenciales: {ex.Message}");
            }

            return null;
        }

        public void ClearSavedCredentials()
        {
            try
            {
                if (File.Exists(_credentialsFileName))
                {
                    File.Delete(_credentialsFileName);
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones (logging, etc.)
                Console.WriteLine($"Error al eliminar las credenciales: {ex.Message}");
            }
        }

        // Métodos de encriptación y desencriptación
        private string EncryptString(string plainText)
        {
            byte[] iv = new byte[16];
            byte[] array;

            using (Aes aes = Aes.Create())
            {
                aes.Key = Encoding.UTF8.GetBytes(_encryptionKey.PadRight(32).Substring(0, 32));
                aes.IV = iv;

                ICryptoTransform encryptor = aes.CreateEncryptor(aes.Key, aes.IV);

                using (MemoryStream memoryStream = new MemoryStream())
                {
                    using (CryptoStream cryptoStream = new CryptoStream((Stream)memoryStream, encryptor, CryptoStreamMode.Write))
                    {
                        using (StreamWriter streamWriter = new StreamWriter((Stream)cryptoStream))
                        {
                            streamWriter.Write(plainText);
                        }

                        array = memoryStream.ToArray();
                    }
                }
            }

            return Convert.ToBase64String(array);
        }

        private string DecryptString(string cipherText)
        {
            byte[] iv = new byte[16];
            byte[] buffer = Convert.FromBase64String(cipherText);

            using (Aes aes = Aes.Create())
            {
                aes.Key = Encoding.UTF8.GetBytes(_encryptionKey.PadRight(32).Substring(0, 32));
                aes.IV = iv;
                ICryptoTransform decryptor = aes.CreateDecryptor(aes.Key, aes.IV);

                using (MemoryStream memoryStream = new MemoryStream(buffer))
                {
                    using (CryptoStream cryptoStream = new CryptoStream((Stream)memoryStream, decryptor, CryptoStreamMode.Read))
                    {
                        using (StreamReader streamReader = new StreamReader((Stream)cryptoStream))
                        {
                            return streamReader.ReadToEnd();
                        }
                    }
                }
            }
        }
    }
}