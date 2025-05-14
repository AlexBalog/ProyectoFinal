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
        public DateTime ExpiryDate { get; set; }
        public string ErrorMessage { get; set; }
        public UserData User { get; set; }
    }

    // Modelo para la información del usuario
    public class UserData
    {
        public int Id { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }
        public string ProfileImage { get; set; }
        public string Role { get; set; }
    }

    // Modelo para guardar credenciales localmente
    public class UserCredentials
    {
        public string Email { get; set; }
    }

    // Modelo para la solicitud de registro
    public class RegisterRequest
    {
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Email { get; set; }
        public string Password { get; set; }
        public string ConfirmPassword { get; set; }
    }

    // Modelo para la respuesta de registro
    public class RegisterResponse
    {
        public bool IsSuccess { get; set; }
        public string Token { get; set; }
        public string RefreshToken { get; set; }
        public UserData User { get; set; }
        public string ErrorMessage { get; set; }
    }

    // Modelo para respuestas genéricas de la API
    public class ApiResponse
    {
        public bool IsSuccess { get; set; }
        public string Message { get; set; }
    }

    // Modelo para respuestas de error
    public class ErrorResponse
    {
        public string Message { get; set; }
        public string[] Errors { get; set; }
    }
}