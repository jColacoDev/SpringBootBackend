package com.apps.springboot.backend.apirest.models.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.apps.springboot.backend.apirest.models.dao.IClientDao;
import com.apps.springboot.backend.apirest.models.entity.Client;

@Service
public class ClientServiceImplm implements IClientService{

	@Autowired
	private IClientDao clientDao;
	
	@Override
	@Transactional(readOnly = true)
	public List<Client> findAll() {
		return (List<Client>) clientDao.findAll();
	}
	@Override
	@Transactional(readOnly = true)
	public Page<Client> findAll(Pageable pageable) {
		return clientDao.findAll(pageable);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Client findById(Long id) {
		return clientDao.findById(id).orElse(null);
	}
	@Override
	@Transactional
	public Client save(Client client) {
		clientDao.save(client);
		
		return client;
	}
	@Override
	@Transactional
	public void delete(Client client) {
		clientDao.delete(client);
	}
	@Override
	@Transactional
	public void deleteId(Long id) {
		clientDao.deleteById(id);
	}
}
