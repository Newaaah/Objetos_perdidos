
package pe.edu.upeu.objetosperdidos.security;

import java.util.Locale;

public final class IdentificadorLogin {

    private IdentificadorLogin() {
    }

    // Identificador interno para usuarios con contraseña.
    public static String construir(
            String tipoDocumento,
            String documentoIdentidad) {

        String tipo = normalizar(tipoDocumento);
        String documento = normalizar(documentoIdentidad);

        if (tipo.isBlank() || documento.isBlank()
                || tipo.contains("|")
                || documento.contains("|")) {

            throw new IllegalArgumentException(
                    "Tipo o numero de documento invalido"
            );
        }

        return tipo + "|" + documento;
    }

    // Separar el identificador interno.
    public static String[] separar(String identificador) {

        if (identificador == null) {
            throw new IllegalArgumentException(
                    "Identificador invalido"
            );
        }

        String[] partes = identificador.split("\\|", -1);

        if (partes.length != 2) {
            throw new IllegalArgumentException(
                    "Identificador invalido"
            );
        }

        String resultado = construir(partes[0], partes[1]);

        return resultado.split("\\|", -1);
    }

    // Preparar el codigo universitario o DNI ingresado.
    public static String normalizarIngreso(String ingreso) {

        String valor = normalizar(ingreso);

        if (!esDni(valor) && !esCodigoUniversitario(valor)) {
            throw new IllegalArgumentException(
                    "Ingrese un DNI de 8 digitos o codigo universitario de 9 caracteres"
            );
        }

        return valor;
    }

    // Identificar formato de DNI.
    public static boolean esDni(String valor) {

        return valor != null
                && valor.matches("[0-9]{8}");
    }

    // Identificar formato de codigo universitario.
    public static boolean esCodigoUniversitario(String valor) {

        return valor != null
                && valor.matches("[A-Z0-9]{9}");
    }

    private static String normalizar(String texto) {

        return texto == null
                ? ""
                : texto.trim().toUpperCase(Locale.ROOT);
    }
}
