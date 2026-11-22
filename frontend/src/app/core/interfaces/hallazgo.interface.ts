export interface MiHallazgo {
    idHallazgo: number;
    idObjeto: number;
    nombreObjeto: string;
    categoria: string;
    estado: string;
    fechaReporte: string;       // "2026-11-21T11:30:00"
    ubicacionDestino?: string;
}

// GET /api/v1/hallazgos/revision/pendientes (HallazgoPendienteRevisionDTO)
export interface HallazgoPendiente {
    idHallazgo: number;
    idObjeto: number;
    idPersonaReporta: number;
    nombreObjeto: string;
    categoria: string;
    marca?: string;
    color?: string;
    descripcion: string;
    ubicacionEncontrado?: string;
    ubicacionDestino?: string;
    fechaReporte: string;
    estado: string;
    imagenRef?: string;
}