package com.apps.springboot.backend.apirest.models.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.apps.springboot.backend.apirest.models.entity.Client;

public interface IClientService {

	public Page<Client> findAll(Pageable pageable);
	public List<Client> findAll();
	public Client findById(Long id);
	public Client save(Client client);
	public void delete(Client currentClient);
	public void deleteId(Long id);
}
