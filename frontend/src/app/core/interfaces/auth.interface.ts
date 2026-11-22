export interface ILoginRequest {
    ingreso: string;     // DNI (8 digitos) o codigo universitario (9)
    password?: string;   // Opcional: el estudiante puede no tener contrasena
}

export interface ILoginResponse {
    token: string;
    tokenType: string;
    idPersona: number;
    rol: string;
}