const express = require('express');
const bcrypt = require('bcrypt');
const UsuariosSchema = require('../models/modelsUsuarios');
const verifyToken = require('../middlewares/authMiddleware'); // Middleware para validar el JWT
const router = express.Router();

const parseFecha = (fecha) => {
  const [day, month, year] = fecha.split('/').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  return date.toISOString();
};

// GET ALL: Obtiene todos los documentos de usuarios (ruta protegida) - La uso para cargar la lista de usuarios sin filtrar en WPF
router.get('/getAll', /*verifyToken,*/ async (req, res) => {
    try {
        const data = await UsuariosSchema.find();
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});


// POST NEW: Crea un nuevo usuario (ruta protegida) - La uso para crear nuevos usuarios en WPF
router.post('/new', /*verifyToken,*/ async (req, res) => {
    try {
      // Hashear la contraseña antes de guardarla
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(req.body.contrasena, saltRounds);
  
      const data = new UsuariosSchema({
        email: req.body.email,
        contrasena: hashedPassword, // Guardar la contraseña cifrada
        nombre: req.body.nombre,
        apellido: req.body.apellido,
        sexo: req.body.sexo,
        fechaNacimiento: req.body.fechaNacimiento,
        foto: req.body.foto,
      });
  
      const dataToSave = await data.save();
      res.status(200).json(dataToSave);
    } catch (error) {
      res.status(400).json({ message: error.message });
    }
  });

// UPDATE: Actualiza un usuario basado en el dni proporcionado (ruta protegida) - La uso para modificar usuarios en WPF
router.patch('/update', verifyToken, async (req, res) => {
  try {
    const id = req.body._id;
    if (!id) {
      return res.status(400).json({ message: "Falta el campo 'id'" });
    }

    // Se crea un objeto para almacenar solo los campos que se enviaron en el request
    const updateFields = {};

    if (req.body.email !== undefined) {
      updateFields.email = req.body.email;
    }

    if (req.body.contrasena !== undefined) {
      let contrasena = req.body.contrasena;
      // Si la contraseña no está cifrada, se cifra
      if (!contrasena.startsWith('$2a$') && !contrasena.startsWith('$2b$')) {
        const saltRounds = 10;
        contrasena = await bcrypt.hash(contrasena, saltRounds);
      }
      updateFields.contrasena = contrasena;
    }

    if (req.body.fechaNacimiento !== undefined) {
      updateFields.fechaNacimiento = parseFecha(req.body.fechaNacimiento);
    }

    if (req.body.nombre !== undefined) {
      updateFields.nombre = req.body.nombre;
    }

    if (req.body.apellido !== undefined) {
      updateFields.apellido = req.body.apellido;
    }

    if (req.body.foto !== undefined) {
      updateFields.foto = req.body.foto;
    }

    if (req.body.sexo !== undefined) {
      updateFields.sexo = req.body.sexo;
    }

    if (req.body.IMC !== undefined) {
      updateFields.IMC = req.body.IMC;
    }

    if (req.body.nivelActividad !== undefined) {
      updateFields.nivelActividad = req.body.nivelActividad;
    }

    if (req.body.caloriasMantenimiento !== undefined) {
      updateFields.caloriasMantenimiento = req.body.caloriasMantenimiento;
    }

    if (req.body.altura !== undefined) {
      updateFields.altura = req.body.altura;
    }

    if (req.body.peso !== undefined) {
      updateFields.peso = req.body.peso;
    }

    if (req.body.objetivoPeso !== undefined) {
      updateFields.objetivoPeso = req.body.objetivoPeso;
    }

    if (req.body.objetivoTiempo !== undefined) {
      updateFields.objetivoTiempo = req.body.objetivoTiempo;
    }

    if (req.body.objetivoCalorias !== undefined) {
      updateFields.objetivoCalorias = req.body.objetivoCalorias;
    }

    if (req.body.entrenamientosFavoritos !== undefined) {
      updateFields.entrenamientosFavoritos = req.body.entrenamientosFavoritos;
    }

    if (req.body.plan !== undefined) {
      updateFields.plan = req.body.plan;
    }

    if (req.body.formulario !== undefined) {
      updateFields.formulario = req.body.formulario;
    }
    
    if (req.body.entrenamientosRealizados !== undefined) {
      updateFields.entrenamientosRealizados = req.body.entrenamientosRealizados
    }

    // Si no se envía ningún campo para actualizar, se informa
    if (Object.keys(updateFields).length === 0) {
      return res.status(400).json({ message: "No se proporcionaron campos para actualizar" });
    }
    // Se realiza la actualización solo de los campos proporcionados
    const resultado = await UsuariosSchema.updateOne(
      { _id: id },
      { $set: updateFields }
    );

    if (resultado.modifiedCount === 0) {
      return res.status(404).json({ message: "Documento no encontrado o datos sin cambios" });
    }

    res.status(200).json({ message: "Documento actualizado exitosamente" });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
});


// DELETE: Elimina un usuario basado en el dni proporcionado (ruta protegida) - La uso para eliminar usuarios en WPF
router.delete('/delete',/* verifyToken,*/ async (req, res) => {
    try {
        const id = req.body._id;
        const usuario = await UsuariosSchema.findById(id);
        if (usuario) {
            await usuario.deleteOne();
        } else {
            return res.status(404).json({ message: "Documento no encontrado" });
        }
        res.status(200).json({ message: `El usuario ${id} se ha eliminado exitosamente` });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

// GET ONE: Obtiene un usuario basado en email y contraseña (ruta protegida) - La uso para obtener todos los datos de un usuario despues de pasar el Login en WPF
router.post('/getOneEmail', verifyToken, async (req, res) => {
    try {
      const { email } = req.body; // Solo se utiliza el email
      const usuarioDB = await UsuariosSchema.findOne({ email });
      if (!usuarioDB) {
        return res.status(404).json({ message: "Documento no encontrado" });
      }
      res.status(200).json(usuarioDB);
    } catch (error) {
      res.status(500).json({ message: error.message });
    }
  });
  
  
// GET FILTER: Filtra usuarios para la API del intermodular (ruta protegida) - La uso para filtrar usuarios en la lista de WPF
router.post('/getFilterInter',verifyToken, async (req, res) => {
    try {
        const condiciones = {};
        if (req.body.rol && req.body.rol.trim() !== "") {
            condiciones.rol = req.body.rol;
        }
        if (req.body.sexo && req.body.sexo.trim() !== "") {
            condiciones.sexo = req.body.sexo;
        }
        if (req.body.fechaNacimiento && req.body.fechaNacimiento.trim() !== "") {
            condiciones.fechaNacimiento = req.body.fechaNacimiento;
        }
        if (req.body.ciudad && req.body.ciudad.trim() !== "") {
            condiciones.ciudad = req.body.ciudad;
        }
        const data = await UsuariosSchema.find(condiciones);
        if (data.length === 0) {
            return res.status(404).json({ message: "Documento no encontrado" });
        }
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});


/* Funciones de android */

/*registro para android*/
router.post('/register', async (req, res) => {
    try {
      const { email, contrasena } = req.body;
      
      // Verificar si ya existe un usuario con el mismo DNI o Email
      const existingUser = await UsuariosSchema.findOne({ email: email });
      
      if (existingUser) {
        return res.status(400).json({ message: "El usuario con ese DNI o Email ya existe" });
      }
      
      // Cifrar la contraseña antes de guardarla
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(contrasena, saltRounds);
      
      // Crear el usuario forzando el rol "cliente"
      const data = new UsuariosSchema({
        nombre: req.body.nombre,
        apellido: req.body.apellido,
        email: req.body.email,
        contrasena: hashedPassword // Guardamos la contraseña cifrada
      });
      
      const dataToSave = await data.save();
      return res.status(200).json(dataToSave);
    } catch (error) {
      return res.status(400).json({ message: error.message });
    }
  });

// GET FILTER
router.post("/getFilter", verifyToken, async (req, res) => {
  try {
      const condiciones = {};

      if (req.body.dni) condiciones.dni = req.body.dni;
      if (req.body.nombre) condiciones.nombre = req.body.nombre;
      if (req.body.apellido) condiciones.apellido = req.body.apellido;
      if (req.body.rol) condiciones.rol = req.body.rol;
      if (req.body.email) condiciones.email = req.body.email;
      if (req.body.ciudad) condiciones.ciudad = req.body.ciudad;
      if (req.body.sexo) condiciones.sexo = req.body.sexo;

      const data = await UsuariosSchema.find(condiciones);
      if (data.length === 0) {
          return res.status(404).json({ message: "Documento no encontrado" });
      }

      res.status(200).json(data);
  } catch (error) {
      res.status(500).json({ message: error.message });
  }
});


module.exports = router;
