using System;
using System.Collections.Generic;

namespace ProyectoFinal.Models
{
    // Modelo para Usuario (basado en tu esquema MongoDB)
    public class Usuario
    {
        public string _id { get; set; }
        public string email { get; set; }
        public string contrasena { get; set; }
        public DateTime? fechaNacimiento { get; set; }
        public string nombre { get; set; }
        public string apellido { get; set; }
        public string foto { get; set; }
        public string sexo { get; set; }
        public double? IMC { get; set; }
        public string nivelActividad { get; set; }
        public double? caloriasMantenimiento { get; set; }
        public double? altura { get; set; }
        public double? peso { get; set; }
        public double? objetivoPeso { get; set; }
        public int? objetivoTiempo { get; set; }
        public double? objetivoCalorias { get; set; }
        public List<string> entrenamientosFavoritos { get; set; } = new List<string>();
        public string plan { get; set; } = "Gratuito";
        public bool formulario { get; set; } = false;
        public List<string> entrenamientosRealizados { get; set; } = new List<string>();

        // Propiedades calculadas para el DataGrid
        public string NombreCompleto => $"{nombre} {apellido}".Trim();
        public string FechaNacimientoTexto => fechaNacimiento?.ToString("dd/MM/yyyy") ?? "No especificada";
        public string EstadoFormulario => formulario ? "Completado" : "Pendiente";
    }

    // Modelo para Entrenamiento
    public class Entrenamiento
    {
        public string _id { get; set; }
        public string categoria { get; set; }
        public string musculoPrincipal { get; set; }
        public string nombre { get; set; }
        public int duracion { get; set; }
        public string foto { get; set; }
        public List<string> musculo { get; set; } = new List<string>();
        public int likes { get; set; } = 0;
        public List<string> ejercicios { get; set; } = new List<string>();
        public string creador { get; set; }
        public bool aprobado { get; set; } = false;
        public bool pedido { get; set; } = false;
        public string motivoRechazo { get; set; }

        // Propiedades calculadas
        public string DuracionTexto => $"{duracion} min";
        public string EstadoAprobacion => aprobado ? "Aprobado" : (pedido ? "Pendiente" : "Sin solicitar");
        public string MusculosTexto => string.Join(", ", musculo);

        public bool CanBeApproved => pedido && !aprobado;
        public bool CanBeRejected => pedido && !aprobado;
    }

    // Modelo para Ejercicio
    public class Ejercicio
    {
        public string _id { get; set; }
        public string nombre { get; set; }
        public string musculo { get; set; }
        public string descripcion { get; set; }
        public string foto { get; set; }
        public List<string> consejos { get; set; } = new List<string>();
        public string tutorial { get; set; }

        // Propiedades calculadas
        public string ConsejosTexto => string.Join(", ", consejos);
        public string DescripcionCorta => descripcion?.Length > 50 ?
            descripcion.Substring(0, 47) + "..." : descripcion;

        public bool TieneConsejos => consejos != null && consejos.Any() && consejos.Any(c => !string.IsNullOrWhiteSpace(c));

        public bool TieneTutorial => !string.IsNullOrWhiteSpace(tutorial);

        public string TutorialTexto => TieneTutorial ? "Disponible" : "No disponible";
    }

    // Modelo para Evento
    public class Evento
    {
        public string _id { get; set; }
        public string nombre { get; set; }
        public string descripcion { get; set; }
        public string tipo { get; set; }

        // Propiedades calculadas
        public string DescripcionCorta => descripcion?.Length > 50 ?
            descripcion.Substring(0, 47) + "..." : descripcion;
    }

    // Modelos para filtros
    public class UsuarioFilter
    {
        public string nombre { get; set; }
        public string apellido { get; set; }
        public string email { get; set; }
        public string sexo { get; set; }
        public string plan { get; set; }
    }

    public class EntrenamientoFilter
    {
        public string nombre { get; set; }
        public string categoria { get; set; }
        public string musculoPrincipal { get; set; }
        public int? duracionMin { get; set; }
        public int? duracionMax { get; set; }
        public string creador { get; set; }
        public bool? aprobado { get; set; }
        public bool? pedido { get; set; }
        public string sortBy { get; set; } = "nombre";
        public string sortDirection { get; set; } = "asc";
    }

    public class EjercicioFilter
    {
        public string nombre { get; set; }
        public string musculo { get; set; }
        public string sortBy { get; set; } = "nombre";
        public string sortDirection { get; set; } = "asc";
    }

    public class EventoFilter
    {
        public string nombre { get; set; }
        public string tipo { get; set; }
    }
}