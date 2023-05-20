package com.apps.springboot.backend.apirest.models.services;

import java.util.List;
import com.apps.springboot.backend.apirest.models.entity.Client;

public interface IClientService {
	
	public List<Client> findAll();
	public Client findById(Long id);
	public void save(Client client);
	public void delete(Client currentClient);
}
