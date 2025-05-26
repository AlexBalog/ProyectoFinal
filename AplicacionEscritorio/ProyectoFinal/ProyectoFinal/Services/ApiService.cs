using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using ProyectoFinal.Models;

namespace ProyectoFinal.Services
{
    public interface IApiService
    {
        Task<LoginResponse> LoginAsync(LoginRequest loginRequest);
        void SetAuthToken(string token);
        void ClearAuthToken();
        Task<bool> VerifyTokenAsync();
    }

    public class ApiService : IApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;

        public ApiService()
        {
            _httpClient = new HttpClient();
            // URL base de tu API Node.js
            _baseUrl = "http://localhost:3000";
            _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));

            // Configurar timeout
            _httpClient.Timeout = TimeSpan.FromSeconds(30);
        }

        public async Task<LoginResponse> LoginAsync(LoginRequest loginRequest)
        {
            try
            {
                // Crear el objeto de solicitud que espera tu API
                var apiRequest = new
                {
                    email = loginRequest.Email,
                    contrasena = loginRequest.Password  // Tu API usa "contrasena" según el modelo
                };

                // Serializar el objeto de solicitud a JSON
                var jsonContent = JsonConvert.SerializeObject(apiRequest);
                var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

                // Realizar solicitud POST al endpoint de login
                var response = await _httpClient.PostAsync($"{_baseUrl}/auth/login", content);

                // Leer la respuesta
                var jsonResponse = await response.Content.ReadAsStringAsync();

                if (response.IsSuccessStatusCode)
                {
                    // La API devolvió éxito
                    try
                    {
                        // Intentar deserializar como respuesta de login exitosa
                        var apiResponse = JsonConvert.DeserializeObject<dynamic>(jsonResponse);

                        // Crear la respuesta de login
                        var loginResponse = new LoginResponse
                        {
                            IsSuccess = true,
                            Token = apiResponse?.token?.ToString(),
                            RefreshToken = apiResponse?.refreshToken?.ToString(),
                            User = new UserData
                            {
                                Email = apiResponse?.user?.email?.ToString() ?? loginRequest.Email,
                                FirstName = apiResponse?.user?.nombre?.ToString(),
                                LastName = apiResponse?.user?.apellido?.ToString(),
                                Id = apiResponse?.user?._id?.ToString(),
                                ProfileImage = apiResponse?.user?.foto?.ToString()
                            }
                        };

                        // Si hay token, configurar para futuras peticiones
                        if (!string.IsNullOrEmpty(loginResponse.Token))
                        {
                            SetAuthToken(loginResponse.Token);
                        }

                        return loginResponse;
                    }
                    catch (JsonException)
                    {
                        // Si no se puede parsear como objeto, asumir que es exitoso pero sin datos estructurados
                        return new LoginResponse
                        {
                            IsSuccess = true,
                            Token = null,
                            ErrorMessage = null
                        };
                    }
                }
                else
                {
                    // Manejar errores específicos de la API
                    try
                    {
                        var errorResponse = JsonConvert.DeserializeObject<dynamic>(jsonResponse);
                        var errorMessage = errorResponse?.message?.ToString() ?? "Error de autenticación";

                        var loginResponse = new LoginResponse
                        {
                            IsSuccess = false
                        };

                        // Determinar el tipo de error específico
                        if (response.StatusCode == System.Net.HttpStatusCode.Unauthorized)
                        {
                            if (errorMessage.ToLower().Contains("usuario") || errorMessage.ToLower().Contains("encontrado"))
                            {
                                loginResponse.ErrorMessage = "Usuario no encontrado";
                                loginResponse.ErrorType = "user_not_found";
                            }
                            else if (errorMessage.ToLower().Contains("contraseña") || errorMessage.ToLower().Contains("credencial"))
                            {
                                loginResponse.ErrorMessage = "Contraseña incorrecta";
                                loginResponse.ErrorType = "wrong_password";
                            }
                            else
                            {
                                loginResponse.ErrorMessage = "Credenciales inválidas";
                                loginResponse.ErrorType = "wrong_password";
                            }
                        }
                        else if (response.StatusCode == System.Net.HttpStatusCode.BadRequest)
                        {
                            loginResponse.ErrorMessage = "Datos de entrada inválidos";
                            loginResponse.ErrorType = "validation";
                        }
                        else
                        {
                            loginResponse.ErrorMessage = errorMessage;
                            loginResponse.ErrorType = "general";
                        }

                        return loginResponse;
                    }
                    catch (JsonException)
                    {
                        // Si no se puede parsear el error, usar mensaje genérico
                        return new LoginResponse
                        {
                            IsSuccess = false,
                            ErrorMessage = $"Error del servidor (Código: {response.StatusCode})",
                            ErrorType = "general"
                        };
                    }
                }
            }
            catch (HttpRequestException ex)
            {
                // Error de conexión
                return new LoginResponse
                {
                    IsSuccess = false,
                    ErrorMessage = "No se pudo conectar con el servidor. Verifique su conexión a internet.",
                    ErrorType = "connection"
                };
            }
            catch (TaskCanceledException ex)
            {
                // Timeout
                return new LoginResponse
                {
                    IsSuccess = false,
                    ErrorMessage = "Tiempo de espera agotado. El servidor no responde.",
                    ErrorType = "connection"
                };
            }
            catch (Exception ex)
            {
                // Error inesperado
                return new LoginResponse
                {
                    IsSuccess = false,
                    ErrorMessage = $"Error inesperado: {ex.Message}",
                    ErrorType = "general"
                };
            }
        }

        public void SetAuthToken(string token)
        {
            if (!string.IsNullOrEmpty(token))
            {
                _httpClient.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", token);
            }
        }

        public void ClearAuthToken()
        {
            _httpClient.DefaultRequestHeaders.Authorization = null;
        }

        // Método para verificar si el token es válido
        public async Task<bool> VerifyTokenAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync($"{_baseUrl}/auth/verify");
                return response.IsSuccessStatusCode;
            }
            catch
            {
                return false;
            }
        }

        // Dispose del HttpClient cuando se destruya el servicio
        public void Dispose()
        {
            _httpClient?.Dispose();
        }
    }
}