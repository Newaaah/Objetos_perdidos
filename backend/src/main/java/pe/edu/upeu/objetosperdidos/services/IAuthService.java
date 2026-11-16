package pe.edu.upeu.objetosperdidos.services;

import pe.edu.upeu.objetosperdidos.dto.AuthResponse;
import pe.edu.upeu.objetosperdidos.dto.LoginRequest;

public interface IAuthService {

    AuthResponse login(LoginRequest request);

}
