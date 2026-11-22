// ConteoEstadoDTO
export interface ConteoEstado {
    estado: string;
    total: number;
}

// ConteoMesDTO
export interface ConteoMes {
    periodo: string;   // ej. "2026-11"
    total: number;
}

// PorcentajeEntregasCategoriaDTO
export interface EntregasCategoria {
    idCategoria: number;
    categoria: string;
    totalCustodias: number;
    totalEntregadas: number;
    porcentaje: number;
}

// ResumenReclamosDTO (procedimiento SP_RESUMEN_RECLAMOS)
export interface ResumenReclamos {
    total: number;
    sinRevisar: number;
    revisados: number;
}

// SeguimientoReclamoDTO (vista VW_SEGUIMIENTO_RECLAMOS)
export interface SeguimientoReclamo {
    idReclamacion: number;
    idObjeto: number;
    categoria: string;
    descripcion: string;
    estadoReclamacion: string;
    fechaSolicitud: string;
    fechaResolucion?: string;
    situacionRevision: string;
}