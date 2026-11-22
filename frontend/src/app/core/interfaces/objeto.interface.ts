export interface Objeto {
    idCustodia: number;
    nombre: string;
    categoria: string;
    marca?: string;
    color?: string;
    ubicacionActual?: string;
    fechaRegistro: string;
    imagenUrl?: string;
}

// GET /api/v1/publico/objetos/entregados (ObjetoEntregadoPublicoDTO)
export interface ObjetoEntregado {
    idEntrega: number;
    idCustodia: number;
    nombre: string;
    categoria: string;
    marca?: string;
    color?: string;
    fechaEntrega: string;
    imagenUrl?: string;
}