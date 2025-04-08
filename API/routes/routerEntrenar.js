const express = require('express');
const router = express.Router();
const modelEntrenar = require('../models/modelsEntrenar'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
    const data = await modelEntrenar.find();
    res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
    });

router.post('/getOneEntrenar', async (req, res) => {
    try{
    const id = req.body._id;
    const data = await modelEntrenar.findOne({ _id: id });
    if (!data) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }
    res.status(200).json(data);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
    });


router.get('/getFilterEntrenar', async (req, res) => {
    try {
        const condiciones = {};

        if (req.body.duracion !== null) {
            condiciones.duracion = req.body.duracion ;
        }

        if (req.body.fechaMin !== undefined || req.body.fechaMax !== undefined) {
            condiciones.fecha = {};
            if (req.body.fechaMin !== undefined) condiciones.fecha.$gte = req.body.fechaMin;
            if (req.body.fechaMax !== undefined) condiciones.fecha.$lte = req.body.fechaMax;
        }
        
        if (req.body.cod_usu !== null) {
            condiciones.cod_usu = req.body.cod_usu
        }
        
        const data = await modelEntrenar.find(condiciones);
        
        if (data.length === 0) {
            return res.status(404).json({ message: 'No hay entrenos hechos con tales características' });
        }
        
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.post('/new', async (req, res) => {
    const data = new modelEntrenar({
        duracion: req.body.duracion,
        fecha: req.body.fecha,
        cod_usu: req.body.cod_usu,
        cod_ent: req.body.cod_ent
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

    const resultado = await modelEntrenar.updateOne(
    { _id: id }, { $set: {
        duracion: req.body.duracion,
        fecha: req.body.fecha,
        cod_usu: req.body.cod_usu,
        cod_ent: req.body.cod_ent
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
    const data = await modelEntrenar.deleteOne({ _id: id })
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