const express = require('express');
const router = express.Router();
const modelEntrenamientos = require('../models/modelsEntrenamientos'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
        const data = await modelEntrenamientos.find();
        res.status(200).json(data);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
    });

router.post('/getOne', async (req, res) => {
    try{
        const id = req.body._id;
        const entrenamientosDB = await modelEntrenamientos.findOne({ _id: id });
    if (!entrenamientosDB) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }
    res.status(200).json(entrenamientosDB);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
    });


router.get('/getFilter', async (req, res) => {
    try {
        const condiciones = {};

        if (req.body.nombre !== null && req.body.nombre.trim() !== "") {
            condiciones.nombre = req.body.nombre;
        }

        if (req.body.categoria !== null && req.body.categoria.trim() !== "") {
            condiciones.categoria = req.body.categoria;
        }

        if (req.body.musculos !== null) {
            condiciones.musculos = req.body.musculos;
        }

        if (req.body.duracion !== null) {
            condiciones.duracion = req.body.duracion;
        }
        
        const data = await modelEntrenamientos.find(condiciones);
        
        if (data.length === 0) {
            return res.status(404).json({ message: 'No hay entrenamientos con tales características' });
        }
        
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.post('/new', async (req, res) => {
    const data = new modelEntrenamientos({
        nombre: req.body.nombre,
        categoria: req.body.categoria,
        musculos: req.body.musculos,
        duracion: req.body.duracion,
        foto: req.body.foto,
        likes: 0,
        ejercicios: req.body.ejercicios,
        creador: req.body.creador,
        pedido: req.body.pedido,
        aprobado: req.body.aprobado,
        motivoRechazo: ""
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

    const resultado = await modelEntrenamientos.updateOne(
    { _id: id }, { $set: {
        nombre: req.body.nombre,
        categoria: req.body.categoria,
        musculos: req.body.musculos,
        duracion: req.body.duracion,
        foto: req.body.foto,
        likes: req.body.likes,
        ejercicios: req.body.ejercicios,
        creador: req.body.creador,
        pedido: req.body.pedido,
        aprobado: req.body.aprobado,
        motivoRechazo: req.body.motivoRechazo
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
    const data = await modelEntrenamientos.deleteOne({ _id: id })
    if (data.deletedCount === 0) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }

    res.status(200).json({ message: `Document with ${id} has been deleted..` })
    }
    catch (error) {
        res.status(400).json({ message: error.message })
    }
    })

router.post('/peticion', async (req, res) => {
    try {
        const id = req.body._id;
        const resultado = await modelEntrenamientos.updateOne(
            { _id: id },
            {$set: {
                pedido: true
                }
            });
        if (resultado.modifiedCount === 0) {
            res.status(404).json({ message: "Entrenamiento no encontrado" })
        }
        res.status(200).json({ message: "Petición del entrenamiento enviada" });
    }
    catch (error) {
        res.status(400).json({ message: error.message })
    }
})

module.exports = router;