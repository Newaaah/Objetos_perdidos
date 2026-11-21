package pe.edu.upeu.objetosperdidos.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.objetosperdidos.dto.ObjetoEntregadoPublicoDTO;
import pe.edu.upeu.objetosperdidos.dto.ObjetoPublicoDTO;
import pe.edu.upeu.objetosperdidos.entity.Entrega;
import pe.edu.upeu.objetosperdidos.entity.Objeto;
import pe.edu.upeu.objetosperdidos.entity.ObjetosCustodiados;
import pe.edu.upeu.objetosperdidos.repository.EntregaRepository;
import pe.edu.upeu.objetosperdidos.repository.ObjetosCustodiadosRepository;

/** Consultas publicas: cualquiera puede ver los objetos sin iniciar sesion. */
@Service
public class CatalogoPublicoServiceImpl implements ICatalogoPublicoService {

    private static final int DIAS_POR_DEFECTO = 90;
    private static final int DIAS_MAXIMO = 365;

    private final ObjetosCustodiadosRepository custodias;
    private final EntregaRepository entregas;
    private final IRecepcionCustodiaService recepcion;

    public CatalogoPublicoServiceImpl(
            ObjetosCustodiadosRepository custodias,
            EntregaRepository entregas,
            IRecepcionCustodiaService recepcion) {
        this.custodias = custodias;
        this.entregas = entregas;
        this.recepcion = recepcion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjetoPublicoDTO> disponibles(Long idCategoria) {
        validarCategoria(idCategoria);
        return custodias.listarDisponiblesPublico(idCategoria)
                .stream()
                .map(c -> {
                    Objeto o = c.getObjeto();
                    return new ObjetoPublicoDTO(
                            c.getIdCustodia(),
                            o.getNombre(),
                            o.getCategoria().getNombre(),
                            o.getMarca(),
                            o.getColor(),
                            c.getUbicacionActual() == null
                                    ? null : c.getUbicacionActual().getNombre(),
                            c.getFechaRegistro(),
                            urlImagen(c));
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjetoEntregadoPublicoDTO> entregados(Long idCategoria, Integer dias) {
        validarCategoria(idCategoria);
        int rango = dias == null ? DIAS_POR_DEFECTO : dias;
        if (rango < 1 || rango > DIAS_MAXIMO) {
            throw new IllegalArgumentException(
                    "El parametro dias debe estar entre 1 y " + DIAS_MAXIMO);
        }
        LocalDateTime desde = LocalDateTime.now(ZoneId.of("America/Lima"))
                .minusDays(rango);
        return entregas.listarEntregadasPublico(desde, idCategoria)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IFileStorageService.Imagen imagen(Long idCustodia) {
        // Solo la foto del OBJETO. La foto de evidencia de la entrega
        // (persona que recibe) no tiene ruta publica.
        return recepcion.leerImagen(idCustodia);
    }

    private ObjetoEntregadoPublicoDTO convertir(Entrega e) {
        ObjetosCustodiados c = e.getCustodia();
        Objeto o = c.getObjeto();
        return new ObjetoEntregadoPublicoDTO(
                e.getIdEntrega(),
                c.getIdCustodia(),
                o.getNombre(),
                o.getCategoria().getNombre(),
                o.getMarca(),
                o.getColor(),
                e.getFechaEntrega(),
                urlImagen(c));
    }

    /** Ruta relativa al context-path (/objetos_perdidos). */
    private static String urlImagen(ObjetosCustodiados c) {
        boolean tieneFoto = (c.getImagen() != null && !c.getImagen().isBlank())
                || c.getHallazgo() != null;
        return tieneFoto
                ? "/api/v1/publico/objetos/" + c.getIdCustodia() + "/imagen"
                : null;
    }

    private static void validarCategoria(Long idCategoria) {
        if (idCategoria != null && idCategoria <= 0) {
            throw new IllegalArgumentException("Categoria invalida");
        }
    }
}
