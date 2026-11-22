export interface CrearPerdidaRequest {
    idCategoria: number;           // obligatorio
    nombre: string;                // obligatorio, max 100
    marca?: string;                // opcional, max 100
    color?: string;                // opcional, max 50
    descripcion: string;           // obligatoria, max 255
    idUbicacionPerdida?: number;   // opcional
    fechaPerdida?: string;         // opcional, formato "2026-11-20"
}

export interface MiPerdida {
    idReportePerdida: number;
    idObjeto: number;
    nombreObjeto: string;
    categoria: string;
    estado: string;            // REPORTADO, RECUPERADO, CANCELADO
    fechaPerdida?: string;     // "2026-11-20" (puede venir null)
    fechaReporte: string;      // "2026-11-21T10:12:20"
    ubicacionPerdida?: string; // puede venir null
}