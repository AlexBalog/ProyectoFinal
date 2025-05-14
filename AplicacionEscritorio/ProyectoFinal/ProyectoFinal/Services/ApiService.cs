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
        Task<ApiResponse> ForgotPasswordAsync(string email);
        Task<RegisterResponse> RegisterAsync(RegisterRequest registerRequest);
    }

    public class ApiService : IApiService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;

        public ApiService()
        {
            _httpClient = new HttpClient();
            // Configura la URL base de tu API
            _baseUrl = "http://localhost:3000/"; // Reemplaza con tu URL real
            _httpClient.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
        }

        public async Task<LoginResponse> LoginAsync(LoginRequest loginRequest)
        {
            try
            {
                // Serializar el objeto de solicitud a JSON
                var jsonContent = JsonConvert.SerializeObject(loginRequest);
                var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

                // Realizar solicitud POST al endpoint de login
                var response = await _httpClient.PostAsync($"{_baseUrl}/auth/login", content);

                // Leer y deserializar la respuesta
                var jsonResponse = await response.Content.ReadAsStringAsync();

                if (response.IsSuccessStatusCode)
                {
                    // Deserializar respuesta exitosa
                    var loginResponse = JsonConvert.DeserializeObject<LoginResponse>(jsonResponse);
                    loginResponse.IsSuccess = true;
                    return loginResponse;
                }
                else
                {
                    // Manejar errores
                    var errorResponse = JsonConvert.DeserializeObject<ErrorResponse>(jsonResponse);
                    return new LoginResponse
                    {
                        IsSuccess = false,
                        ErrorMessage = errorResponse?.Message ?? "Error de autenticación"
                    };
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones
                return new LoginResponse
                {
                    IsSuccess = false,
                    ErrorMessage = $"Error al comunicarse con el servidor: {ex.Message}"
                };
            }
        }

        public async Task<ApiResponse> ForgotPasswordAsync(string email)
        {
            try
            {
                // Crear el objeto de solicitud
                var request = new
                {
                    Email = email
                };

                // Serializar a JSON
                var jsonContent = JsonConvert.SerializeObject(request);
                var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

                // Realizar solicitud POST al endpoint de recuperación de contraseña
                var response = await _httpClient.PostAsync($"{_baseUrl}/auth/forgot-password", content);

                // Leer y deserializar la respuesta
                var jsonResponse = await response.Content.ReadAsStringAsync();

                if (response.IsSuccessStatusCode)
                {
                    return new ApiResponse
                    {
                        IsSuccess = true,
                        Message = "Se ha enviado un enlace de recuperación a su correo electrónico"
                    };
                }
                else
                {
                    // Manejar errores
                    var errorResponse = JsonConvert.DeserializeObject<ErrorResponse>(jsonResponse);
                    return new ApiResponse
                    {
                        IsSuccess = false,
                        Message = errorResponse?.Message ?? "Error al procesar la solicitud"
                    };
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones
                return new ApiResponse
                {
                    IsSuccess = false,
                    Message = $"Error al comunicarse con el servidor: {ex.Message}"
                };
            }
        }

        public async Task<RegisterResponse> RegisterAsync(RegisterRequest registerRequest)
        {
            try
            {
                // Serializar el objeto de solicitud a JSON
                var jsonContent = JsonConvert.SerializeObject(registerRequest);
                var content = new StringContent(jsonContent, Encoding.UTF8, "application/json");

                // Realizar solicitud POST al endpoint de registro
                var response = await _httpClient.PostAsync($"{_baseUrl}/auth/register", content);

                // Leer y deserializar la respuesta
                var jsonResponse = await response.Content.ReadAsStringAsync();

                if (response.IsSuccessStatusCode)
                {
                    // Deserializar respuesta exitosa
                    var registerResponse = JsonConvert.DeserializeObject<RegisterResponse>(jsonResponse);
                    registerResponse.IsSuccess = true;
                    return registerResponse;
                }
                else
                {
                    // Manejar errores
                    var errorResponse = JsonConvert.DeserializeObject<ErrorResponse>(jsonResponse);
                    return new RegisterResponse
                    {
                        IsSuccess = false,
                        ErrorMessage = errorResponse?.Message ?? "Error al registrar usuario"
                    };
                }
            }
            catch (Exception ex)
            {
                // Manejar excepciones
                return new RegisterResponse
                {
                    IsSuccess = false,
                    ErrorMessage = $"Error al comunicarse con el servidor: {ex.Message}"
                };
            }
        }
    }
}