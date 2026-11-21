package pe.edu.upeu.objetosperdidos.services;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import pe.edu.upeu.objetosperdidos.dto.MiEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.EntregaRegistradaDTO;
import pe.edu.upeu.objetosperdidos.dto.EstadoCustodiaEntregaDTO;
import pe.edu.upeu.objetosperdidos.dto.RegistrarEntregaRequest;

public interface IEntregaService {
    List<MiEntregaDTO> misEntregas(Long idPersona);
    EstadoCustodiaEntregaDTO estadoCustodia(Long idCustodia);
    EntregaRegistradaDTO registrar(RegistrarEntregaRequest datos,
                                  MultipartFile fotografia,
                                  Long idPersonaPersonal);
    EntregaRegistradaDTO consultar(Long idEntrega);
    IFileStorageService.Imagen leerEvidencia(Long idEntrega);
}
