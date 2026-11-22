// GET /api/v1/custodias/recepcion/pendientes (PendienteRecepcionCustodiaDTO)
export interface PendienteRecepcion {
    idHallazgo: number;
    idObjeto: number;
    nombreObjeto: string;
    idUbicacionDestino?: number;
    ubicacionDestino?: string;
    fechaReporte: string;
}

// Respuesta al registrar (CustodiaRegistradaDTO)
export interface CustodiaRegistrada {
    idCustodia: number;
    idHallazgo?: number;
    idObjeto: number;
    nombreObjeto: string;
    idPersonalRegistra: number;
    idUbicacionActual: number;
    ubicacionActual: string;
    idPersonaEntrega?: number;
    fechaRegistro: string;
    observacionRecepcion?: string;
    origen: string;
    imagenRef?: string;
}