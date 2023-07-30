package com.apps.springboot.backend.apirest.controllers;

// import java.util.Date;
import java.util.List;
// import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
// import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apps.springboot.backend.apirest.models.entity.Client;
import com.apps.springboot.backend.apirest.models.services.IClientService;

import jakarta.validation.Valid;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap; 

@CrossOrigin(origins= {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
public class ClientRestController {

	@Autowired
	private IClientService clientService;
	
	private final Logger log = LoggerFactory.getLogger(ClientRestController.class);

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
			String previousPicName = deletedClient.getPicture();
			if(previousPicName !=null && previousPicName.length()>0){
				Path previousRoutePic = Paths.get("uploads").resolve(previousPicName).toAbsolutePath();
				File previousPicFile = previousRoutePic.toFile();
				if(previousPicFile.exists() && previousPicFile.canRead()){
					previousPicFile.delete();
				}
			}

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

	@PostMapping("/clients/upload")
	public ResponseEntity<?> upload(
		@RequestParam("file") MultipartFile file,
		@RequestParam("id") Long id
	) {
		Map<String, Object> response = new HashMap<>();
		Client client = clientService.findById(id);
	
		if (!file.isEmpty()) {
			String originalFilename = file.getOriginalFilename();
			if (originalFilename != null) {
				String fileName = UUID.randomUUID().toString() + "__" + originalFilename.replace(" ", "_");
				Path fileRoute = Paths.get("uploads").resolve(fileName).toAbsolutePath();
				log.info(fileRoute.toString());

				try {
					Files.copy(file.getInputStream(), fileRoute);
				} catch (Exception e) {
					response.put("message", "Error uploading image: " + fileName);
					response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
					return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				String previousPicName = client.getPicture();
				if(previousPicName !=null && previousPicName.length()>0){
					Path previousRoutePic = Paths.get("uploads").resolve(previousPicName).toAbsolutePath();
					File previousPicFile = previousRoutePic.toFile();
					if(previousPicFile.exists() && previousPicFile.canRead()){
						previousPicFile.delete();
					}
				}

				client.setPicture(fileName);
				clientService.save(client);
	
				response.put("message", "Success uploading image: " + fileName);
				response.put("client: ", client);
			} else {
				response.put("message", "Error uploading image: original filename is null");
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} else {
			response.put("message", "Error uploading image: file is empty");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/uploads/img/{picName:.+}")
	public ResponseEntity<Resource> seePic(@PathVariable String picName){

		Path fileRoute = Paths.get("uploads").resolve(picName).toAbsolutePath();
		log.info(fileRoute.toString());
		
		Resource resource= null;

		try {
			resource= new UrlResource(fileRoute.toUri());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if(resource==null || !resource.exists() || !resource.isReadable()){
			throw new RuntimeException("Error uploading image: "+ picName);
		}
		HttpHeaders header= new HttpHeaders();
		header.add(HttpHeaders.CONTENT_DISPOSITION, 
			"attachment; filename=\""+resource.getFilename()+"\"");

		return new ResponseEntity<Resource>(resource, header, HttpStatus.OK);
	}
}

