package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.ClienteVO;

import java.util.List;
import java.util.Optional;

public interface ClienteDAO extends GenericDAO<ClienteVO, Long> {
    List<ClienteVO> buscarPorEstado(String estado);

    Optional<ClienteVO> buscarPorCorreo(String correo);

    Optional<ClienteVO> buscarPorTelefono(String telefono);
}
