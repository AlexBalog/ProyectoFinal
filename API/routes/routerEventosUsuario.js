const express = require('express');
const router = express.Router();
const modelEventosUsuario = require('../models/modelsEventosUsuario'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
    const data = await modelEventosUsuario.find();
    res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
    });

router.post('/getOneEventoUsuario', async (req, res) => {
    try{
    const id = req.body._id;
    const data = await modelEventosUsuario.findOne({ _id: id });
    if (!data) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }
    res.status(200).json(data);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
    });


router.get('/getFilterEventosUsuarios', async (req, res) => {
    try {
        const condiciones = {};

        if (req.body.usuario !== null) {
            condiciones.usuario = req.body.usuario;
        }
        if (req.body.fecha !== null) {
            condiciones.fecha = req.body.fecha;
        }
        if (req.body.evento !== null) {
            condiciones.evento = req.body.evento;
        }
        
        const data = await modelEventosUsuario.find(condiciones);
        
        if (data.length === 0) {
            return res.status(404).json({ message: 'No hay ejercicios con tales características' });
        }
        
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.post('/new', async (req, res) => {
    const data = new modelEventosUsuario({
        evento: req.body.evento,
        usuario: req.body.usuario,
        fecha: req.body.fecha,
        hora: req.body.hora
    })

    try {
    const dataToSave = await data.save();
    res.status(200).json(dataToSave);
    }
    catch (error) {
    res.status(400).json({message: error.message});
    }
    });

router.patch("/update", async (req, res) => {
    try {
    const id = req.body._id;

    const resultado = await modelEventosUsuario.updateOne(
    { _id: id }, { $set: {
        evento: req.body.evento,
        usuario: req.body.usuario,
        fecha: req.body.fecha,
        hora: req.body.hora
    }});
    
    if (resultado.modifiedCount === 0) {
        return res.status(404).json({ message: "Documento no encontrado" });
    }
    
    res.status(200).json({ message: "Documento actualizado exitosamente"
    });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

router.delete('/delete', async (req, res) => {
    try {
    const id = req.body._id;
    const data = await modelEventosUsuario.deleteOne({ _id: id })
    if (data.deletedCount === 0) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }

    res.status(200).json({ message: `Document with ${id} has been deleted..` })
    }
    catch (error) {
        res.status(400).json({ message: error.message })
    }
    })

module.exports = router;