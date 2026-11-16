package com.uniremington.cspizza.service;

import com.uniremington.cspizza.model.vo.ClienteVO;
import com.uniremington.cspizza.model.entity.ClienteEntity;
import com.uniremington.cspizza.model.repository.ClienteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClienteService extends AbstractCrudService<ClienteVO, ClienteEntity> {
    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository repository) {
        super(repository);
        this.clienteRepository = repository;
    }

    @Override protected Long idOf(ClienteVO value) { return value.getIdCliente(); }
    @Override protected ClienteEntity map(ClienteVO value, ClienteEntity entity) {
        if (value == null) throw new IllegalArgumentException("El cuerpo de la solicitud es obligatorio.");
        if (entity == null) entity = new ClienteEntity();
        if (value.getNombre() == null || value.getNombre().isBlank()
                || value.getApellido() == null || value.getApellido().isBlank()) {
            throw new IllegalArgumentException("El nombre y apellido son obligatorios.");
        }
        entity.setNombre(value.getNombre().trim());
        entity.setApellido(value.getApellido().trim());
        entity.setTelefono(value.getTelefono());
        entity.setCorreo(value.getCorreo());
        entity.setDireccion(value.getDireccion());
        entity.setEstado(value.getEstado() == null || value.getEstado().isBlank() ? "ACTIVO" : value.getEstado());
        if (entity.getFechaRegistro() == null) {
            entity.setFechaRegistro(value.getFechaRegistro() == null ? LocalDateTime.now() : value.getFechaRegistro());
        }
        return entity;
    }
    @Override protected ClienteVO view(ClienteEntity entity) {
        ClienteVO value = new ClienteVO();
        value.setIdCliente(entity.getIdCliente());
        value.setNombre(entity.getNombre());
        value.setApellido(entity.getApellido());
        value.setTelefono(entity.getTelefono());
        value.setCorreo(entity.getCorreo());
        value.setDireccion(entity.getDireccion());
        value.setEstado(entity.getEstado());
        value.setFechaRegistro(entity.getFechaRegistro());
        return value;
    }
    @Transactional(readOnly=true) public List<ClienteVO> buscarPorEstado(String estado) { return clienteRepository.findAllByEstado(estado).stream().map(this::view).toList(); }
    @Transactional(readOnly=true) public Optional<ClienteVO> buscarPorCorreo(String correo) { return clienteRepository.findFirstByCorreo(correo).map(this::view); }
    @Transactional(readOnly=true) public Optional<ClienteVO> buscarPorTelefono(String telefono) { return clienteRepository.findFirstByTelefono(telefono).map(this::view); }
    protected List<ClienteVO> filtered(Predicate<ClienteEntity> predicate) { return clienteRepository.findAll().stream().filter(predicate).map(this::view).toList(); }
}
