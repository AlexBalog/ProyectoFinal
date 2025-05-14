const jwt = require('jsonwebtoken');

function verifyToken(req, res, next) {
  console.log("Entra en verifyToken: Verificando token...");
  // Se asume que el token se envía en la cabecera Authorization con el formato: Bearer <token>
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ message: 'Acceso denegado. No se proporcionó token.' });
  }

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    req.user = decoded; // Puedes acceder a los datos del usuario en req.user
    next();
  } catch (error) {
    console.error("Error al verificar el token, que no le apetece");
    res.status(400).json({ message: 'Token inválido' });
  }
}

module.exports = verifyToken;