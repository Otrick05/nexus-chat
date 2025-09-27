package com.example.cadenasuministros.cadenasuministros.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cadenasuministros.cadenasuministros.model.Permiso;
import com.example.cadenasuministros.cadenasuministros.service.PermisoService;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {

    private PermisoService permisoService;

    public PermisoController(PermisoService permisoService){

        this.permisoService = permisoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LEER_PERMISO')")
    public ResponseEntity <List<Permiso>> listaPermisos() {
        return new ResponseEntity<>(permisoService.listaPermisos(),HttpStatus.ACCEPTED);
    }
    
    @GetMapping("/{id}")
	@PreAuthorize("hasAuthority('LEER_PERMISO')")
    public ResponseEntity<Permiso> getPermisoById(@PathVariable int id){
		Optional<Permiso> permiso = permisoService.getById(id);
		if(permiso.isPresent()) {
			return new ResponseEntity<>(permiso.get(),HttpStatus.OK);
			
		}else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
	}

    @PostMapping
    @PreAuthorize("hasAuthority('CREAR_PERMISO')")    
    public ResponseEntity <Permiso> crear(@RequestBody Permiso permiso) {
        Permiso savePermiso = permisoService.crear(permiso);
        return new ResponseEntity<>(savePermiso,HttpStatus.CREATED);

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACTUALIZAR_PERMISO')")
    public Permiso actualizar(@PathVariable Long id, @RequestBody Permiso permiso) {
        return permisoService.actualizar(id, permiso);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BORRAR_PERMISO')")
    public ResponseEntity<String> eliminarPermiso(@PathVariable Long id) {
        if (permisoService.permisoExistente(id)) {
            permisoService.borrarById(id);
            return ResponseEntity.ok("Permiso eliminado: " + id);
        }else{
            return new ResponseEntity<>("No existe permiso con el id" + id, HttpStatus.NOT_FOUND);
        }
       
    }
    
}
