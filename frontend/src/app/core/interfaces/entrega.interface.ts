export interface EstadoCustodiaEntrega {
    idCustodia: number;
    idObjeto: number;
    nombreObjeto: string;
    disponibleParaEntrega: boolean;
    idEntrega?: number;   // si ya se entrego, viene el id de esa entrega
}

export interface EntregaRegistrada {
    idEntrega: number;
}

export interface MiEntrega {
    idEntrega: number;
    idCustodia: number;
    idReportePerdida?: number;
    nombreObjeto: string;
    fechaEntrega: string;
}