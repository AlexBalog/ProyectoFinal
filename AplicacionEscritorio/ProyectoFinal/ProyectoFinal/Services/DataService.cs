using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using ProyectoFinal.Models;

namespace ProyectoFinal.Services
{
    public interface IDataService
    {
        // Usuarios
        Task<List<Usuario>> GetAllUsuariosAsync();
        Task<List<Usuario>> GetUsuariosFiltradosAsync(UsuarioFilter filter);
        Task<Usuario> CreateUsuarioAsync(Usuario usuario);
        Task<bool> UpdateUsuarioAsync(Usuario usuario);
        Task<bool> DeleteUsuarioAsync(string id);

        // Entrenamientos
        Task<List<Entrenamiento>> GetAllEntrenamientosAsync();
        Task<List<Entrenamiento>> GetEntrenamientosFiltradosAsync(EntrenamientoFilter filter);
        Task<Entrenamiento> CreateEntrenamientoAsync(Entrenamiento entrenamiento);
        Task<bool> UpdateEntrenamientoAsync(Entrenamiento entrenamiento);
        Task<bool> DeleteEntrenamientoAsync(string id);

        // Ejercicios
        Task<List<Ejercicio>> GetAllEjerciciosAsync();
        Task<List<Ejercicio>> GetEjerciciosFiltradosAsync(EjercicioFilter filter);
        Task<Ejercicio> CreateEjercicioAsync(Ejercicio ejercicio);
        Task<bool> UpdateEjercicioAsync(Ejercicio ejercicio);
        Task<bool> DeleteEjercicioAsync(string id);

        // Eventos
        Task<List<Evento>> GetAllEventosAsync();
        Task<List<Evento>> GetEventosFiltradosAsync(EventoFilter filter);
        Task<Evento> CreateEventoAsync(Evento evento);
        Task<bool> UpdateEventoAsync(Evento evento);
        Task<bool> DeleteEventoAsync(string id);
    }

    public class DataService : IDataService
    {
        private readonly HttpClient _httpClient;
        private readonly string _baseUrl;

        public DataService(IApiService apiService)
        {
            _httpClient = new HttpClient();
            _baseUrl = "https://api-y9fu.onrender.com";
            _httpClient.DefaultRequestHeaders.Accept.Add(new System.Net.Http.Headers.MediaTypeWithQualityHeaderValue("application/json"));

            // Configurar el token de autorización
            var token = new UserService().GetToken();
            if (!string.IsNullOrEmpty(token))
            {
                _httpClient.DefaultRequestHeaders.Authorization =
                    new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);
            }
        }

        #region Usuarios
        public async Task<List<Usuario>> GetAllUsuariosAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync($"{_baseUrl}/usuarios/getAll");
                if (response.IsSuccessStatusCode)
                {
                    var json = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Usuario>>(json) ?? new List<Usuario>();
                }
                return new List<Usuario>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error obteniendo usuarios: {ex.Message}");
                return new List<Usuario>();
            }
        }

        public async Task<List<Usuario>> GetUsuariosFiltradosAsync(UsuarioFilter filter)
        {
            try
            {
                var json = JsonConvert.SerializeObject(filter);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/usuarios/getFilter", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Usuario>>(responseJson) ?? new List<Usuario>();
                }
                return new List<Usuario>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error filtrando usuarios: {ex.Message}");
                return new List<Usuario>();
            }
        }

        public async Task<Usuario> CreateUsuarioAsync(Usuario usuario)
        {
            try
            {
                var json = JsonConvert.SerializeObject(usuario);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/usuarios/new", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<Usuario>(responseJson);
                }
                return null;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error creando usuario: {ex.Message}");
                return null;
            }
        }

        public async Task<bool> UpdateUsuarioAsync(Usuario usuario)
        {
            try
            {
                var json = JsonConvert.SerializeObject(usuario);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PatchAsync($"{_baseUrl}/usuarios/update", content);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error actualizando usuario: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteUsuarioAsync(string id)
        {
            try
            {
                var requestData = new { _id = id };
                var json = JsonConvert.SerializeObject(requestData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var request = new HttpRequestMessage(HttpMethod.Delete, $"{_baseUrl}/usuarios/delete")
                {
                    Content = content
                };

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error eliminando usuario: {ex.Message}");
                return false;
            }
        }
        #endregion

        #region Entrenamientos
        public async Task<List<Entrenamiento>> GetAllEntrenamientosAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync($"{_baseUrl}/entrenamientos/getAll");
                if (response.IsSuccessStatusCode)
                {
                    var json = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Entrenamiento>>(json) ?? new List<Entrenamiento>();
                }
                return new List<Entrenamiento>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error obteniendo entrenamientos: {ex.Message}");
                return new List<Entrenamiento>();
            }
        }

        public async Task<List<Entrenamiento>> GetEntrenamientosFiltradosAsync(EntrenamientoFilter filter)
        {
            try
            {
                var json = JsonConvert.SerializeObject(filter);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/entrenamientos/getFilter", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Entrenamiento>>(responseJson) ?? new List<Entrenamiento>();
                }
                return new List<Entrenamiento>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error filtrando entrenamientos: {ex.Message}");
                return new List<Entrenamiento>();
            }
        }

        public async Task<Entrenamiento> CreateEntrenamientoAsync(Entrenamiento entrenamiento)
        {
            try
            {
                var json = JsonConvert.SerializeObject(entrenamiento);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/entrenamientos/new", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<Entrenamiento>(responseJson);
                }
                return null;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error creando entrenamiento: {ex.Message}");
                return null;
            }
        }

        public async Task<bool> UpdateEntrenamientoAsync(Entrenamiento entrenamiento)
        {
            try
            {
                var json = JsonConvert.SerializeObject(entrenamiento);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PatchAsync($"{_baseUrl}/entrenamientos/update", content);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error actualizando entrenamiento: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteEntrenamientoAsync(string id)
        {
            try
            {
                var requestData = new { _id = id };
                var json = JsonConvert.SerializeObject(requestData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var request = new HttpRequestMessage(HttpMethod.Delete, $"{_baseUrl}/entrenamientos/delete")
                {
                    Content = content
                };

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error eliminando entrenamiento: {ex.Message}");
                return false;
            }
        }
        #endregion

        #region Ejercicios
        public async Task<List<Ejercicio>> GetAllEjerciciosAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync($"{_baseUrl}/ejercicios/getAll");
                if (response.IsSuccessStatusCode)
                {
                    var json = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Ejercicio>>(json) ?? new List<Ejercicio>();
                }
                return new List<Ejercicio>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error obteniendo ejercicios: {ex.Message}");
                return new List<Ejercicio>();
            }
        }

        public async Task<List<Ejercicio>> GetEjerciciosFiltradosAsync(EjercicioFilter filter)
        {
            try
            {
                var json = JsonConvert.SerializeObject(filter);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/ejercicios/getFilter", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Ejercicio>>(responseJson) ?? new List<Ejercicio>();
                }
                return new List<Ejercicio>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error filtrando ejercicios: {ex.Message}");
                return new List<Ejercicio>();
            }
        }

        public async Task<Ejercicio> CreateEjercicioAsync(Ejercicio ejercicio)
        {
            try
            {
                var json = JsonConvert.SerializeObject(ejercicio);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/ejercicios/new", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<Ejercicio>(responseJson);
                }
                return null;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error creando ejercicio: {ex.Message}");
                return null;
            }
        }

        public async Task<bool> UpdateEjercicioAsync(Ejercicio ejercicio)
        {
            try
            {
                var json = JsonConvert.SerializeObject(ejercicio);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PatchAsync($"{_baseUrl}/ejercicios/update", content);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error actualizando ejercicio: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteEjercicioAsync(string id)
        {
            try
            {
                var requestData = new { _id = id };
                var json = JsonConvert.SerializeObject(requestData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var request = new HttpRequestMessage(HttpMethod.Delete, $"{_baseUrl}/ejercicios/delete")
                {
                    Content = content
                };

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error eliminando ejercicio: {ex.Message}");
                return false;
            }
        }
        #endregion

        #region Eventos
        public async Task<List<Evento>> GetAllEventosAsync()
        {
            try
            {
                var response = await _httpClient.GetAsync($"{_baseUrl}/eventos/getAll");
                if (response.IsSuccessStatusCode)
                {
                    var json = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Evento>>(json) ?? new List<Evento>();
                }
                return new List<Evento>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error obteniendo eventos: {ex.Message}");
                return new List<Evento>();
            }
        }

        public async Task<List<Evento>> GetEventosFiltradosAsync(EventoFilter filter)
        {
            try
            {
                var json = JsonConvert.SerializeObject(filter);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/eventos/getFilter", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<List<Evento>>(responseJson) ?? new List<Evento>();
                }
                return new List<Evento>();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error filtrando eventos: {ex.Message}");
                return new List<Evento>();
            }
        }

        public async Task<Evento> CreateEventoAsync(Evento evento)
        {
            try
            {
                var json = JsonConvert.SerializeObject(evento);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PostAsync($"{_baseUrl}/eventos/new", content);
                if (response.IsSuccessStatusCode)
                {
                    var responseJson = await response.Content.ReadAsStringAsync();
                    return JsonConvert.DeserializeObject<Evento>(responseJson);
                }
                return null;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error creando evento: {ex.Message}");
                return null;
            }
        }

        public async Task<bool> UpdateEventoAsync(Evento evento)
        {
            try
            {
                var json = JsonConvert.SerializeObject(evento);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var response = await _httpClient.PatchAsync($"{_baseUrl}/eventos/update", content);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error actualizando evento: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteEventoAsync(string id)
        {
            try
            {
                var requestData = new { _id = id };
                var json = JsonConvert.SerializeObject(requestData);
                var content = new StringContent(json, Encoding.UTF8, "application/json");

                var request = new HttpRequestMessage(HttpMethod.Delete, $"{_baseUrl}/eventos/delete")
                {
                    Content = content
                };

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error eliminando evento: {ex.Message}");
                return false;
            }
        }
        #endregion
    }
}