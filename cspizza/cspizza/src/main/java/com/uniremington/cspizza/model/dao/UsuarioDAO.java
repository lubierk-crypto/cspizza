package com.uniremington.cspizza.model.dao;

import com.uniremington.cspizza.model.vo.UsuarioVO;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO extends GenericDAO<UsuarioVO, Long> {
    Optional<UsuarioVO> buscarPorNombreUsuario(String nombreUsuario);

    Optional<UsuarioVO> buscarPorCorreo(String correo);

    List<UsuarioVO> buscarPorRol(String rol);

    List<UsuarioVO> buscarPorEstado(String estado);
}
