package pe.edu.upeu.objetosperdidos.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import pe.edu.upeu.objetosperdidos.dto.CustodiaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.PendienteRecepcionCustodiaDTO;
import pe.edu.upeu.objetosperdidos.dto.RecepcionCustodiaRequest;
import pe.edu.upeu.objetosperdidos.dto.RecepcionDirectaCustodiaRequest;

public interface IRecepcionCustodiaService {

    List<PendienteRecepcionCustodiaDTO> pendientes();

    // Mantiene el POST JSON anterior para no romper a clientes existentes.
    CustodiaRegistradaDTO recibir(RecepcionCustodiaRequest solicitud, Long idPersonaPersonal);

    // Permite adjuntar una fotografia nueva cuando el hallazgo no tenga una valida.
    CustodiaRegistradaDTO recibirConImagen(RecepcionCustodiaRequest solicitud,
                                           MultipartFile imagen, Long idPersonaPersonal);

    // El personal recibe directamente un objeto sin reporte de hallazgo.
    CustodiaRegistradaDTO recibirDirectamente(RecepcionDirectaCustodiaRequest solicitud,
                                               MultipartFile imagen, Long idPersonaPersonal);

    CustodiaRegistradaDTO consultar(Long idCustodia);

    AlmacenamientoImagenCustodiaService.Imagen leerImagen(Long idCustodia);
}
