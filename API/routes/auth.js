const express = require('express');
const router = express.Router();
const jwt = require('jsonwebtoken');
const usuariosSchema = require('../models/modelsUsuarios');
const bcrypt = require('bcryptjs'); // Importa bcrypt

router.post('/login', async (req, res) => {
  const { email, contrasena } = req.body;
  try {
    // Buscar el usuario por email
    const usuarioDB = await usuariosSchema.findOne({ email });
    if (!usuarioDB) {
      return res.status(401).json({ message: 'Credenciales inválidas gmail' });
    }

    // Comparar la contraseña en texto plano con la contraseña cifrada almacenada
    const isPasswordValid = await bcrypt.compare(contrasena, usuarioDB.contrasena);
    if (!isPasswordValid) {
      return res.status(401).json({ message: 'Credenciales inválidas contrasena' });
    }

    // Crear la carga útil (payload) para el token
    const payload = {
      id: usuarioDB._id,
      email: usuarioDB.email,
      plan: usuarioDB.plan,
      iat: Math.floor(Date.now() / 1000)
    };

    // Generar el token (con expiración de 24 horas)
    const token = jwt.sign(
      payload, 
      process.env.JWT_SECRET, 
      { 
        expiresIn: '24h',
        algorithm: 'HS256'
      });

    const decoded = jwt.decode(token);
    console.log('Token generado. Expira en:', new Date(decoded.exp * 1000).toISOString());

    res.json({ token });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
});

module.exports = router;
