package com.apps.springboot.backend.apirest.controllers;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apps.springboot.backend.apirest.models.entity.Client;
import com.apps.springboot.backend.apirest.models.services.IClientService;

import jakarta.validation.Valid;
import java.util.HashMap; 

@CrossOrigin(origins= {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClientRestController {

	@Autowired
	private IClientService clientService;
	
	@GetMapping("/clients")
	public List<Client> index(){
		return clientService.findAll();
	}
	@GetMapping("/clients/page/{page}")
	public Page<Client> index(@PathVariable Integer page){
		return clientService.findAll(PageRequest.of(page, 6));
	}

	
	@GetMapping("/clients/{id}")
	public ResponseEntity<?> show(@PathVariable Long id) {
		Client client = null;
		Map<String, Object> response = new HashMap<>();

		try {
			client = clientService.findById(id);
		}
		catch(DataAccessException e) {
			response.put("message", "Error accessing the database!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
		if(client == null) {
			response.put("message", "Client ID:".concat(id.toString()).concat(" doesen't exist in database!"));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<Client>(client, HttpStatus.OK);
	}
	
	@PostMapping("/clients")
	public ResponseEntity<?> create(@Valid @RequestBody Client client, BindingResult result) {
		Client newClient = null;
		Map<String, Object> response = new HashMap<>();
		
		if(result.hasErrors()) {			
			/*
			List<String> errors = new ArrayList<>();
			for(FieldError err: result.getFieldErrors()) {
				errors.add("Field '"+err.getField()+"' "+err.getDefaultMessage());
			}
			*/
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(err->"Field '"+err.getField()+"' "+err.getDefaultMessage())
					.collect(Collectors.toList());
			
			response.put("errors", errors);
			
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			newClient = this.clientService.save(client);
		} 
		catch(DataAccessException e) {
			response.put("message", "Error inserting the database!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Success inserting the database!");
		response.put("client", newClient);
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}
	
	@PutMapping("/clients/{id}")
	public ResponseEntity<?> update(@Valid @RequestBody Client client, BindingResult result, @PathVariable Long id) {
		Client currentClient = clientService.findById(id);
		Client updatedClient = null;
		
		Map<String, Object> response = new HashMap<>();
		
		if(result.hasErrors()) {			
			List<String> errors = result.getFieldErrors()
					.stream()
					.map(err->"Field '"+err.getField()+"' "+err.getDefaultMessage())
					.collect(Collectors.toList());
			
			response.put("errors", errors);
			
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		if(currentClient == null) {
			response.put("message", "Error: Can't edit, Client ID:".concat(id.toString()).concat(" doesen't exist in database!"));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
		}
		try {
			currentClient.setFirstName(client.getFirstName());
			currentClient.setLastName(client.getLastName());
			currentClient.setEmail(client.getEmail());
			currentClient.setCreatedAt(client.getCreatedAt());
			
			updatedClient = this.clientService.save(currentClient);
		} 
		catch(DataAccessException e) {
			response.put("message", "Error updating the database!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Success updating the database!");
		response.put("client", updatedClient);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@DeleteMapping("/clients/{id}")
	//@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<?> delete(@PathVariable Long id) {
		Client deletedClient = null;
		Map<String, Object> response = new HashMap<>();

		try {
			deletedClient = this.clientService.findById(id);
			this.clientService.delete(deletedClient);
		}
		catch(DataAccessException e) {
			response.put("message", "Error deleting the database!");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.put("message", "Success deleting the database!");
		response.put("client: ", deletedClient);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);

	}
}

