const express = require('express');
const bcrypt = require('bcrypt');
const UsuariosSchema = require('../models/modelsUsuarios');
const verifyToken = require('../middlewares/authMiddleware'); // Middleware para validar el JWT
const router = express.Router();

// GET ALL: Obtiene todos los documentos de usuarios (ruta protegida) - La uso para cargar la lista de usuarios sin filtrar en WPF
router.get('/getAll', verifyToken, async (req, res) => {
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
        fecha_nac: req.body.fecha_nac,
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
    const { dni } = req.body;
    if (!dni) {
      return res.status(400).json({ message: "Falta el campo 'dni'" });
    }

    // Se crea un objeto para almacenar solo los campos que se enviaron en el request
    const updateFields = {};

    // Si se envía el nombre, se añade al objeto
    if (req.body.nombre !== undefined) {
      updateFields.nombre = req.body.nombre;
    }
    // Si se envía el apellido, se añade al objeto
    if (req.body.apellido !== undefined) {
      updateFields.apellido = req.body.apellido;
    }
    // Si se envía el rol, se añade al objeto
    if (req.body.rol !== undefined) {
      updateFields.rol = req.body.rol;
    }
    // Si se envía la fecha de nacimiento, se añade al objeto
    if (req.body.fecha_nac !== undefined) {
      updateFields.fecha_nac = req.body.fecha_nac;
    }
    // Si se envía la ciudad, se añade al objeto
    if (req.body.ciudad !== undefined) {
      updateFields.ciudad = req.body.ciudad;
    }
    // Si se envía el sexo, se añade al objeto
    if (req.body.sexo !== undefined) {
      updateFields.sexo = req.body.sexo;
    }
    // Si se envía la imagen, se añade al objeto
    if (req.body.imagen !== undefined) {
      updateFields.imagen = req.body.imagen;
    }
    // Si se envía el email, se añade al objeto
    if (req.body.email !== undefined) {
      updateFields.email = req.body.email;
    }
    // Si se envía la contraseña, se cifra (si es necesario) y se añade al objeto
    if (req.body.contrasena !== undefined) {
      let contrasena = req.body.contrasena;
      // Si la contraseña no está cifrada, se cifra
      if (!contrasena.startsWith('$2a$') && !contrasena.startsWith('$2b$')) {
        const saltRounds = 10;
        contrasena = await bcrypt.hash(contrasena, saltRounds);
      }
      updateFields.contrasena = contrasena;
    }

    // Si no se envía ningún campo para actualizar, se informa
    if (Object.keys(updateFields).length === 0) {
      return res.status(400).json({ message: "No se proporcionaron campos para actualizar" });
    }

    // Se realiza la actualización solo de los campos proporcionados
    const resultado = await UsuariosSchema.updateOne(
      { dni },
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
  


  // GET ONE DNI
  router.post('/getOneDni', verifyToken, async (req, res) => {
    try {
      const { dni } = req.body;
      const usuarioDB = await UsuariosSchema.findOne({dni });
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
            condiciones.fecha_nac = req.body.fechaNacimiento;
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
      const { dni, email, contrasena } = req.body;
      
      // Verificar si ya existe un usuario con el mismo DNI o Email
      const existingUser = await UsuariosSchema.findOne({
        $or: [{ dni: dni }, { email: email }]
      });
      
      if (existingUser) {
        return res.status(400).json({ message: "El usuario con ese DNI o Email ya existe" });
      }
      
      // Cifrar la contraseña antes de guardarla
      const saltRounds = 10;
      const hashedPassword = await bcrypt.hash(contrasena, saltRounds);
      
      // Crear el usuario forzando el rol "cliente"
      const data = new UsuariosSchema({
        dni: req.body.dni,
        nombre: req.body.nombre,
        apellido: req.body.apellido,
        rol: "cliente", // Se asigna siempre "cliente"
        email: req.body.email,
        contrasena: hashedPassword, // Guardamos la contraseña cifrada
        fecha_nac: req.body.fecha_nac,
        ciudad: req.body.ciudad,
        sexo: req.body.sexo,
        imagen: req.body.imagen // Se asigna siempre una imagen por defecto, si aplica
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
