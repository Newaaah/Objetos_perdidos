export interface Reclamo {
    idReclamo: number;
    tipo: string;
    estado: string;          // PENDIENTE, EN_REVISION, APROBADO, RECHAZADO
    idEntrega?: number;
    idCustodia?: number;
    idHallazgo?: number;
    descripcion: string;
    resolucion?: string;     // respuesta del personal al cerrar
    tieneEvidencia: boolean;
    fechaRegistro: string;
    fechaResolucion?: string;
}