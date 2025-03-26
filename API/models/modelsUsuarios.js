const mongoose = require("mongoose");
const UsuariosSchema = mongoose.Schema({
    _id: { type: String }, 
    email: {
        required: true,
        type: String
    }, 
    contrasena: {
        required: true,
        type: String
    },
    fecha_nac: {
        required: true,
        type: Date
    }, 
    nombre: {
        required: true,
        type: String
    }, 
    apellido: {
        required: true,
        type: String
    }, 
    foto: {
        required: true,
        type: String
    },
    sexo: {
        required: true,
        type: String
    },
    IMC: {
        type: Number
    }, 
    altura: {
        type: Number
    }, 
    peso: {
        type: Number
    }, 
    objetivo_peso: {
        type: Number
    }, 
    objetivo_tiem: {
        type: Number
    }, 
    objetivo_cal: {
        type: Number
    },
    ent_fav: {
        type: [String],
        default: []
    },
    plan: {
        type: String,
        default: "Gratuito"
    },
    formulario: {
        type: Boolean,
        default: false
    }
}, {__v: false});

const CodigoLiberado = require('./modelsCodigosLiberados');

UsuariosSchema.pre('deleteOne', { document: true, query: false }, async function (next) {
    await CodigoLiberado.create({ codigo: this._id, tipo: 'usuarios' });
    next();
});

UsuariosSchema.pre('save', async function (next) {
    const usuario = this;
    if (!usuario.isNew) return next();

    try {
        let nuevoID = "U00001";  // Código inicial si no hay liberados

        // Buscar código liberado del tipo 'usuarios'
        const codigoLiberado = await CodigoLiberado.findOne({ tipo: 'usuarios' }).sort({ codigo: 1 }).exec();
        
        if (codigoLiberado) {
            nuevoID = codigoLiberado.codigo;
            await CodigoLiberado.deleteOne({ codigo: nuevoID, tipo: 'usuarios' }); // Eliminarlo de la lista de liberados
        } else {
            // Si no hay códigos liberados, generar uno nuevo
            const ultimoUsuario = await this.constructor.findOne({}).sort({ _id: -1 }).exec();
            if (ultimoUsuario && ultimoUsuario._id) {
                const match = ultimoUsuario._id.match(/^U(\d{5})$/);
                if (match) {
                    const ultimoNumero = parseInt(match[1], 10);
                    const nuevoNumero = (ultimoNumero + 1).toString().padStart(5, '0');
                    nuevoID = `U${nuevoNumero}`;
                }
            }
        }

        usuario._id = nuevoID;
        next();
    } catch (err) {
        next(err);
    }
});

module.exports = mongoose.model("usuarios", UsuariosSchema);