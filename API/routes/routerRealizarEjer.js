const express = require('express');
const router = express.Router();
const modelRealizarEjer = require('../models/modelsRealizarEjer'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
    const data = await modelRealizarEjer.find();
    res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
    });

router.post('/getOneRealizarEjer', async (req, res) => {
    try{
    const id = req.body._id;
    const data = await modelRealizarEjer.findOne({ _id: id });
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
    const data = new modelRealizarEjer({
        cod_ent: req.body.cod_ent,
        cod_eje: req.body.cod_eje,
        repeticiones: req.body.repeticiones,
        series: req.body.series,
        peso: req.body.peso
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

    const resultado = await modelRealizarEjer.updateOne(
    { _id: id }, { $set: {
        cod_ent: req.body.cod_ent,
        cod_eje: req.body.cod_eje,
        repeticiones: req.body.repeticiones,
        series: req.body.series,
        peso: req.body.peso
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
    const data = await modelRealizarEjer.deleteOne({ _id: id })
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