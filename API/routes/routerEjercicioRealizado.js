const express = require('express');
const router = express.Router();
const modelEjercicioRealizado = require('../models/modelsEjercicioRealizado'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
    const data = await modelEjercicioRealizado.find();
    res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
    });

router.post('/getOne', async (req, res) => {
    try{
    const id = req.body._id;
    const data = await modelEjercicioRealizado.findOne({ _id: id });
    if (!data) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }
    res.status(200).json(data);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
});

router.post('/new', async (req, res) => {
    const data = new modelEjercicioRealizado({
        entrenamiento: req.body.entrenamiento,
        entrenamientoRealizado: req.body.entrenamientoRealizado,
        ejercicio: req.body.ejercicio,
        nombre: req.body.nombre
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

    const resultado = await modelEjercicioRealizado.updateOne(
    { _id: id }, { $set: {
        entrenamiento: req.body.entrenamiento,
        entrenamientoRealizado: req.body.entrenamientoRealizado,
        ejercicio: req.body.ejercicio,
        nombre: req.body.nombre,
        seriesRealizadas: req.body.seriesRealizadas
    },});
    
    if (resultado.modifiedCount === 0) {
        return res.status(404).json({ message: "Documento no encontrado" });
    }
    
    res.status(200).json({ message: "Documento actualizado exitosamente"
    });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});


router.patch("/update/:id", async (req, res) => {
    try {
    const id = req.params.id;

    const resultado = await modelEjercicioRealizado.updateOne(
    { _id: id }, { $set: {
        entrenamiento: req.body.entrenamiento,
        entrenamientoRealizado: req.body.entrenamientoRealizado,
        ejercicio: req.body.ejercicio,
        nombre: req.body.nombre,
        seriesRealizadas: req.body.seriesRealizadas
    },});
    
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
    const data = await modelEjercicioRealizado.deleteOne({ _id: id })
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