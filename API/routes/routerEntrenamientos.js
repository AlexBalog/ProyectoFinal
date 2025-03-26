const express = require('express');
const router = express.Router();
const ModelUser = require('../models/modelsEntrenamientos'); 
//middleware para acceder 
router.get('/getAll', async (req, res) => {
    try{
    const data = await ModelUser.find();
    res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
    });

router.post('/getOne', async (req, res) => {
    try{
    const id = req.body._id;
    const habitacionesDB = await ModelUser.findOne({ _id: id });
    if (!habitacionesDB) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }
    res.status(200).json(habitacionesDB);
    }
    catch(error){
        res.status(500).json({message: error.message});
    }
    });


    router.get('/getOne', async (req, res) => {
        try{
        const id = req.query._id;
        const habitacionesDB = await ModelUser.findOne({ _id: id });
        console.log(habitacionesDB);
        if (!habitacionesDB) {
            return res.status(404).json({ message: 'Documento no encontrado' });
        }
        res.status(200).json(habitacionesDB);
        }
        catch(error){
            res.status(500).json({message: error.message});
        }
        });



// getOne get

router.get('/getOne/:id', async (req, res) => {
    try {
        const habitacion = await ModelUser.findOne({ _id: req.params.id });
        if (!habitacion) {
            return res.status(404).json({ message: 'Habitación no encontrada' });
        }
    } catch (error) {
        console.log("err");
        res.status(500).json({ message: error.message });
    }
});

// getOne getCondiciones

router.get('/getOneHuespedes/:id', async (req, res) => {
    try {
        const { numHuespedes } = req.query;
        const habitacion = await ModelUser.findOne({ 
            _id: req.params.id, 
            ...(numHuespedes && { huespedes: { $gte: parseInt(numHuespedes) } }) 
        });
        
        if (!habitacion) {
            return res.status(404).json({ message: 'Habitación no encontrada o no cumple los filtros' });
        }

        res.status(200).json(habitacion);
    } catch (error) {
        console.error("Error en getOne:", error);
        res.status(500).json({ message: error.message });
    }
});

router.get('/getFilterHuespedes', async (req, res) => {
    try {
        const condiciones = {};

        if (req.query.huespedes) {
            condiciones.huespedes = { $gte: parseInt(req.query.huespedes)} ;
        }
        if (req.query.camaExtra && req.query.camaExtra != null){
            condiciones.camaExtra = req.query.camaExtra;
        }
        if (req.query.cuna && req.query.cuna != null){
            condiciones.cuna = req.query.cuna;
        }
        condiciones.baja = false
        const data = await ModelUser.find(condiciones);
        
        if (data.length === 0) {
            return res.status(404).json({ message: 'No hay habitaciones disponibles para ese número de huéspedes' });
        }
        
        res.status(200).json(data);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});




router.post('/getFilter2', async (req, res) => {
    try{
        const condiciones = {};
        if (req.body.huespedes) condiciones.huespedes = req.body.huespedes;
        if (req.body.ofertaMin !== undefined || req.body.ofertaMax !== undefined) {
            condiciones.oferta = {};
            if (req.body.ofertaMin !== undefined) condiciones.oferta.$gte = req.body.ofertaMin;
            if (req.body.ofertaMax !== undefined) condiciones.oferta.$lte = req.body.ofertaMax;
        }
        if (req.body.baja) condiciones.baja = req.body.baja;
        if (req.body.camaExtra) condiciones.camaExtra = req.body.camaExtra;
        if (req.body.cuna) condiciones.cuna = req.body.cuna;
        const data = await ModelUser.find(condiciones);
        if (data.length === 0) {
            return res.status(404).json({ message: 'Documento no encontrado' });
        }
        res.status(200).json(data);
    }
    catch(error){
    res.status(500).json({message: error.message});
    }
});

router.post('/new', async (req, res) => {
    const data = new ModelUser({
        nombre: req.body.nombre,
        huespedes: req.body.huespedes,
        descripcion: req.body.descripcion,
        imagen: req.body.imagen,
        precio: req.body.precio,
        oferta: req.body.oferta,
        finOferta: req.body.finOferta,
        camaExtra: req.body.camaExtra,
        baja: req.body.baja,
        cuna: req.body.cuna
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

    const resultado = await ModelUser.updateOne(
    { _id: id }, { $set: {
        nombre: req.body.nombre,
        huespedes: req.body.huespedes,
        descripcion: req.body.descripcion,
        imagen: req.body.imagen,
        precio: req.body.precio,
        oferta: req.body.oferta,
        finOferta: req.body.finOferta,
        camaExtra: req.body.camaExtra,
        baja: req.body.baja,
        cuna: req.body.cuna
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
    const data = await ModelUser.deleteOne({ _id: id })
    if (data.deletedCount === 0) {
        return res.status(404).json({ message: 'Documento no encontrado' });
    }

    res.status(200).json({ message: `Document with ${id} has been deleted..` })
    }
    catch (error) {
        res.status(400).json({ message: error.message })
    }
    })

    router.get('/getFilterAndroid', async (req, res) => {
        try {
            const condiciones = {};
    
            if (req.query.nombre) condiciones.nombre = req.query.nombre;
            if (req.query.huespedes != -1) condiciones.huespedes = parseInt(req.query.huespedes, 10);
            if (req.query.camaExtra) condiciones.camaExtra = req.query.camaExtra === 'true';
            if (req.query.cuna) condiciones.cuna = req.query.cuna === 'true';
    
            console.log("Condiciones de búsqueda:", condiciones); // Para depuración
    
            const data = await ModelUser.find(condiciones);
            const filteredData = data.filter(element => element.baja !== true);
            if (filteredData.length === 0) {
                return res.status(404).json({ message: 'No se encontraron habitaciones' });
            }
    
            res.status(200).json(filteredData);
        } catch (error) {
            res.status(500).json({ message: error.message });
        }
    });

module.exports = router;