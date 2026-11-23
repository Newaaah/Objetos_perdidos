// Cada candidato (SugerenciaCoincidenciaDTO)
export interface Sugerencia {
    origen: string;            // de donde viene el candidato (custodia, hallazgo...)
    idReferencia: number;
    categoria: string;
    nivelCoincidencia: string; // ALTA, MEDIA, BAJA...
}

export interface ResultadoSugerencias {
    idReportePerdida: number;
    estadoReporte: string;
    sugerencias: Sugerencia[];
    aviso?: string;
}

export interface EvaluacionCoincidencia {
    idReportePerdida: number;
    idHallazgo: number;
    estadoPerdida: string;
    estadoHallazgo: string;
    nombrePerdido: string;
    marcaPerdido?: string;
    colorPerdido?: string;
    descripcionPerdido?: string;
    nombreHallado: string;
    marcaHallado?: string;
    colorHallado?: string;
    descripcionHallado?: string;
    similitud: number;
    cobertura: number;
    prioridad: number;
    objetoEntregado: boolean;
    decisionExistente?: string;   // si ya se decidio antes
    aviso?: string;
}

export interface CoincidenciaRevisada {
    idCoincidencia: number;
    idReportePerdida: number;
    idHallazgo: number;
    estado: string;
    puntaje?: number;
    idPersonalRevisor: number;
    fechaRevision: string;
    observacion: string;
}

// ===== Listas para elegir la pareja (PERSONAL / ADMIN) =====
export interface PerdidaCandidata {
    idReportePerdida: number;
    idObjeto: number;
    nombreObjeto: string;
    categoria: string;
    estado: string;
    fechaPerdida?: string;
    fechaReporte: string;
    ubicacionPerdida?: string;
}

export interface HallazgoCandidato {
    idHallazgo: number;
    idObjeto: number;
    nombreObjeto: string;
    categoria: string;
    estado: string;
    fechaReporte: string;
    ubicacionDestino?: string;
}

export interface CandidatosRevision {
    perdidas: PerdidaCandidata[];
    hallazgos: HallazgoCandidato[];
}