using System;

namespace ProyectoFinal.Models
{
    // Modelo para la solicitud de inicio de sesión
    public class LoginRequest
    {
        public string Email { get; set; }
        public string Password { get; set; }
    }

    // Modelo para la respuesta de inicio de sesión
    public class LoginResponse
    {
        public bool IsSuccess { get; set; }
        public string Token { get; set; }
        public string RefreshToken { get; set; }
        public DateTime? ExpiryDate { get; set; }
        public string ErrorMessage { get; set; }
        public string ErrorType { get; set; }  // ← Esta propiedad faltaba
        public UserData User { get; set; }
    }

    // Modelo para la información del usuario (basado en tu esquema de MongoDB)
    public class UserData
    {
        public string FullName => $"{FirstName} {LastName}".Trim();
        public string Id { get; set; }  // _id del esquema
        public string FirstName { get; set; }  // nombre
        public string LastName { get; set; }   // apellido
        public string Email { get; set; }
        public string ProfileImage { get; set; }  // foto
        public string Role { get; set; }
        public string Sexo { get; set; }
        public DateTime? FechaNacimiento { get; set; }
        public double? IMC { get; set; }
        public string NivelActividad { get; set; }
        public double? CaloriasMantenimiento { get; set; }
        public double? Altura { get; set; }
        public double? Peso { get; set; }
        public double? ObjetivoPeso { get; set; }
        public int? ObjetivoTiempo { get; set; }
        public double? ObjetivoCalorias { get; set; }
        public string[] EntrenamientosFavoritos { get; set; }
        public string Plan { get; set; }
        public bool? Formulario { get; set; }
        public string[] EntrenamientosRealizados { get; set; }
    }

    // Modelo para guardar credenciales localmente
    public class UserCredentials
    {
        public string Email { get; set; }
    }

    // Modelo para respuestas genéricas de la API
    public class ApiResponse
    {
        public bool IsSuccess { get; set; }
        public string Message { get; set; }
        public object Data { get; set; }
    }

    // Modelo para respuestas de error
    public class ErrorResponse
    {
        public string Message { get; set; }
        public string[] Errors { get; set; }
        public int? StatusCode { get; set; }
    }

    // Modelo para token de autenticación
    public class AuthToken
    {
        public string Token { get; set; }
        public string RefreshToken { get; set; }
        public DateTime ExpiryDate { get; set; }
        public string TokenType { get; set; } = "Bearer";
    }

    // Modelo para verificación de token
    public class TokenVerificationResponse
    {
        public bool Valid { get; set; }
        public string UserId { get; set; }
        public string Message { get; set; }
    }
}