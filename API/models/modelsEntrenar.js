const mongoose = require("mongoose");
const EntrenarSchema = mongoose.Schema({
    _id: { type: String },
    cod_usu: {
        required: true,
        type: String
    },
    cod_ent: {
        required: true,
        type: String
    },
    duracion: {
        required: true,
        type: Number
    },
    fecha: {
        required: true,
        type: Date
    }
});

const CodigoLiberado = require('./modelsCodigosLiberados');

EntrenarSchema.pre('deleteOne', { document: true, query: false }, async function (next) {
    await CodigoLiberado.create({ codigo: this._id, tipo: 'entrenar' });
    next();
});

EntrenarSchema.pre('save', async function (next) {
    const entrenar = this;
    if (!entrenar.isNew) return next();

    try {
        let nuevoID = "EA00001";  // Código inicial si no hay liberados

        // Buscar código liberado del tipo 'entrenar'
        const codigoLiberado = await CodigoLiberado.findOne({ tipo: 'entrenar' }).sort({ codigo: 1 }).exec();
        
        if (codigoLiberado) {
            nuevoID = codigoLiberado.codigo;
            await CodigoLiberado.deleteOne({ codigo: nuevoID, tipo: 'entrenar' }); // Eliminarlo de la lista de liberados
        } else {
            // Si no hay códigos liberados, generar uno nuevo
            const ultimoEntrenar = await this.constructor.findOne({}).sort({ _id: -1 }).exec();
            if (ultimoEntrenar && ultimoEntrenar._id) {
                const match = ultimoEntrenar._id.match(/^EA(\d{5})$/);
                if (match) {
                    const ultimoNumero = parseInt(match[1], 10);
                    const nuevoNumero = (ultimoNumero + 1).toString().padStart(5, '0');
                    nuevoID = `EA${nuevoNumero}`;
                }
            }
        }

        entrenar._id = nuevoID;
        next();
    } catch (err) {
        next(err);
    }
});

module.exports = mongoose.model("entrenar", EntrenarSchema);