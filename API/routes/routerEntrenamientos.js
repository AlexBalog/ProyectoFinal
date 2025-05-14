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


router.post('/getFilter', async (req, res) => {
    try {
        const {
            nombre,
            categoria,
            musculoPrincipal,
            duracionMin,
            duracionMax,
            creador,
            aprobado,
            pedido,
            sortBy = 'nombre',
            sortDirection = 'asc'
        } = req.body;

        const condiciones = {};

        if (nombre && nombre.trim() !== "") {
            condiciones.nombre = { $regex: nombre.trim(), $options: 'i' }; // búsqueda parcial e insensible a mayúsculas
        }

        if (categoria && categoria.trim() !== "") {
            condiciones.categoria = categoria.trim();
        }

        if (musculoPrincipal && musculoPrincipal.trim() !== "") {
            condiciones.musculoPrincipal = {$regex: musculoPrincipal.trim(), $options: 'i' }; 
        }

        if (duracionMin != null || duracionMax != null) {
            condiciones.duracion = {};
            if (duracionMin != null) condiciones.duracion.$gte = Number(duracionMin);
            if (duracionMax != null) condiciones.duracion.$lte = Number(duracionMax);
        }

        if (creador && creador.trim() !== "") {
            condiciones.creador = creador.trim();
        }

        if (typeof aprobado === "boolean") {
            condiciones.aprobado = aprobado;
        }

        if (typeof pedido === "boolean") {
            condiciones.pedido = pedido;
        }

        // Ordenamiento
        const sortOptions = {};
        sortOptions[sortBy] = sortDirection === 'desc' ? -1 : 1;

        const data = await modelEntrenamientos.find(condiciones).sort(sortOptions);

        if (data.length === 0) {
            return res.status(404).json({ message: 'No se encontraron entrenamientos con esas características' });
        }

        res.status(200).json(data);
    } catch (error) {
        console.error("Error en /getFilter:", error);
        res.status(500).json({ message: error.message });
    }
});


router.post('/new', async (req, res) => {
    const data = new modelEntrenamientos({
        nombre: req.body.nombre,
        categoria: req.body.categoria,
        musculo: req.body.musculo,
        musculoPrincipal: req.body.musculoPrincipal,
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
        musculoPrincipal: req.body.musculoPrincipal,
        musculo: req.body.musculo,
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