package com.example.cadenasuministros.cadenasuministros.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.example.cadenasuministros.cadenasuministros.model.Permiso;
import com.example.cadenasuministros.cadenasuministros.repository.PermisoRepository;

@Service
public class PermisoService {

    private PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository){
        this.permisoRepository = permisoRepository;
    }

    public List<Permiso> listaPermisos() {
        
        return permisoRepository.findAll();
    }

    public Permiso obtenerPermisoById(Long id){

        return permisoRepository.findById(id)
            .orElseThrow( () ->   new RuntimeException("Permiso no encontrado"));
    }

    public Permiso crear(Permiso permiso) {
       
       return permisoRepository.save(permiso);
    }

    public Permiso actualizar(Long id, Permiso permiso) {
        
        Permiso permisoExistente = obtenerPermisoById(id);
        if (permisoRepository.findByNombre(permiso.getNombre()).isPresent()) {
            throw new RuntimeException("El nombre del permiso ya existe");
        }else{
        permisoExistente.setNombre(permiso.getNombre());
        return permisoRepository.save(permisoExistente);
        }        
    }

    public String borrarById(Long id){
        if(permisoRepository.existsById(id)){
             permisoRepository.deleteById(id);
            return "Borrado exitoso "+ id;
        }else{
            return "Permiso no Encontrado";
        }
       
    }


    public boolean permisoExistente(Long id){
        
        return permisoRepository.existsById(id);
    }

    public Optional<Permiso> getById(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

}
