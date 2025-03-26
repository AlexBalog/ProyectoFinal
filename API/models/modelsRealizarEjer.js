const mongoose = require("mongoose");
const RealizarEjerSchema = mongoose.Schema({
    _id: { type: String },
    cod_ent: {
        required: true,
        type: String
    },
    cod_eje: {
        required: true,
        type: String
    },
    repeticiones: {
        required: true,
        type: Number
    },
    series: {
        required: true,
        type: Number
    },
    peso: {
        required: true,
        type: Number
    }
})

const CodigoLiberado = require('./modelsCodigosLiberados');

RealizarEjerSchema.pre('deleteOne', { document: true, query: false }, async function (next) {
    await CodigoLiberado.create({ codigo: this._id, tipo: 'realizarejer' });
    next();
});

RealizarEjerSchema.pre('save', async function (next) {
    const realizar = this;
    if (!realizar.isNew) return next();

    try {
        let nuevoID = "RE00001";  // Código inicial si no hay liberados

        // Buscar código liberado del tipo 'realizarejer'
        const codigoLiberado = await CodigoLiberado.findOne({ tipo: 'realizarejer' }).sort({ codigo: 1 }).exec();
        
        if (codigoLiberado) {
            nuevoID = codigoLiberado.codigo;
            await CodigoLiberado.deleteOne({ codigo: nuevoID, tipo: 'realizarejer' }); // Eliminarlo de la lista de liberados
        } else {
            // Si no hay códigos liberados, generar uno nuevo
            const ultimoRealizar = await this.constructor.findOne({}).sort({ _id: -1 }).exec();
            if (ultimoRealizar && ultimoRealizar._id) {
                const match = ultimoRealizar._id.match(/^RE(\d{5})$/);
                if (match) {
                    const ultimoNumero = parseInt(match[1], 10);
                    const nuevoNumero = (ultimoNumero + 1).toString().padStart(5, '0');
                    nuevoID = `RE${nuevoNumero}`;
                }
            }
        }

        realizar._id = nuevoID;
        next();
    } catch (err) {
        next(err);
    }
});

module.exports = mongoose.model("realizarejer", RealizarEjerSchema);