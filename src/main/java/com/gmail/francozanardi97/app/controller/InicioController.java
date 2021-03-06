package com.gmail.francozanardi97.app.controller;

import java.sql.SQLException;

import org.jpl7.JPLException;
import org.jpl7.PrologException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gmail.francozanardi97.app.domain.NotificacionError;
import com.gmail.francozanardi97.app.dto.NodoTree;
import com.gmail.francozanardi97.app.dto.ProgramaUsuario;
import com.gmail.francozanardi97.app.dto.RamaTree;
import com.gmail.francozanardi97.app.dto.SustitutionTree;
import com.gmail.francozanardi97.app.service.ServiceNotificacionError;
import com.gmail.francozanardi97.app.treeSLD.ArbolSLD;
import com.gmail.francozanardi97.app.treeSLD.ManejadorArbolesSLD;


@Controller
public class InicioController {

	@Autowired
	private ServiceNotificacionError serviceNotifError;
	
	@Autowired
	private ManejadorArbolesSLD manejadorArbol;

	@RequestMapping(value="/", method=RequestMethod.GET)
	public String getInicio(Model model) {
		return "inicio";
	}
		
	@RequestMapping(value="/", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> crearSLD(@ModelAttribute ProgramaUsuario p) {
		String error = "";
		if(p.getQueryProlog().endsWith(".")) {
			p.setQueryProlog(p.getQueryProlog().substring(0, p.getQueryProlog().length()-1));
		}
		
		try {
			if(p != null && !p.getQueryProlog().isEmpty()) {
				return new ResponseEntity<>(manejadorArbol.agregarArbolSLD(p), HttpStatus.OK);
			}	
		} catch (PrologException e) {
			error = e.term().arg(1).name();
		} catch (JPLException e) {
			error = e.getMessage();
		} catch (Throwable e) {
			e.printStackTrace();
			error = "Fallo inesperado";
		} 
		
		return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value="/notificarError", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<Void> notificarError(@RequestParam("id") String id, @RequestParam("descripcion_error") String descripcionError) {
		ArbolSLD arbol = manejadorArbol.getArbolSLD(id);
		ProgramaUsuario pu;
		NotificacionError ne;
		
		if(arbol != null) {
			pu = arbol.getProgramaUsuario();
			ne = new NotificacionError(pu, descripcionError);
			try {
				serviceNotifError.guardarNotificacion(ne);
				
				return new ResponseEntity<>(HttpStatus.OK);
			} catch (SQLException e) {
				System.out.println(e.getMessage());
			}
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value="/getSolucion", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<NodoTree> getSolucion(@RequestParam("id") String id) {
		ArbolSLD arbol = manejadorArbol.getArbolSLD(id);
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getSolucion(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value="/getRaiz", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<NodoTree> getRaiz(@RequestParam("id") String id) {
		ArbolSLD arbol = manejadorArbol.getArbolSLD(id);
			
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getRaiz(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
	}
	
	
	@RequestMapping(value="/avanzarFotograma", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<Integer> avanzarFotograma(@RequestParam("id") String id) {
		ArbolSLD arbol = manejadorArbol.getArbolSLD(id);
		
		if(arbol != null) {
			arbol.siguienteFotograma();
			
			return new ResponseEntity<>(arbol.getFotograma(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(-1, HttpStatus.BAD_REQUEST);

	}
	
	@RequestMapping(value="/getNodos", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<NodoTree[]> getNodos(@RequestParam("id") String id) {
		ArbolSLD arbol =  manejadorArbol.getArbolSLD(id);
		
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getNodosActuales(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new NodoTree[] {}, HttpStatus.BAD_REQUEST);		
	}
	
	@RequestMapping(value="/getRamas", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<RamaTree[]> getRamas(@RequestParam("id") String id) {
		ArbolSLD arbol =  manejadorArbol.getArbolSLD(id);
		
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getRamasActuales(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new RamaTree[] {}, HttpStatus.BAD_REQUEST);	
	}
	
	@RequestMapping(value="/getRamasCut", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<RamaTree[]> getRamasCut(@RequestParam("id") String id) {
		ArbolSLD arbol =  manejadorArbol.getArbolSLD(id);
		
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getRamasCutActuales(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new RamaTree[] {}, HttpStatus.BAD_REQUEST);	
	}
	
	@RequestMapping(value="/getSustituciones", method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<SustitutionTree[]> getSustituciones(@RequestParam("id") String id) {
		ArbolSLD arbol =  manejadorArbol.getArbolSLD(id);
		
		if(arbol != null) {
			return new ResponseEntity<>(arbol.getSustitucionesActuales(), HttpStatus.OK);
		}
		
		return new ResponseEntity<>(new SustitutionTree[] {}, HttpStatus.BAD_REQUEST);	
	}
	
	@RequestMapping(value="/eliminarArbol", method=RequestMethod.POST)
	public @ResponseBody void eliminarArbol(@RequestParam("id") String id) {
		manejadorArbol.eliminarArbol(id);
	}
	
}
