export interface Notificacion {
    idNotificacion: number;
    tipo: string;
    titulo: string;
    mensaje: string;
    fechaCreacion: string;
    leida: boolean;
    idReportePerdida?: number;
    idHallazgo?: number;
}