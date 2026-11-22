export interface PersonaResumen {
    idPersona: number;
    nombres: string;
    apellidos: string;
    documentoIdentidad: string;
    codigoUni?: string;   // vacio si no es estudiante
    carrera?: string;
}