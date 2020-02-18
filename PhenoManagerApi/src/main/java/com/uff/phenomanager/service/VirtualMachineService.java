package com.uff.phenomanager.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.domain.Environment;
import com.uff.phenomanager.domain.VirtualMachine;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.repository.VirtualMachineRepository;
import com.uff.phenomanager.service.core.ApiRestService;

@Service
public class VirtualMachineService extends ApiRestService<VirtualMachine, VirtualMachineRepository> {
	
	@Autowired
	private VirtualMachineRepository virtualMachineRepository;
	
	@Override
	protected VirtualMachineRepository getRepository() {
		return virtualMachineRepository;
	}
	
	@Override
	protected Class<VirtualMachine> getEntityClass() {
		return VirtualMachine.class;
	}
	
	public Set<VirtualMachine> save(Set<VirtualMachine> virtualMachines, Environment environment) throws ApiException {
		Set<VirtualMachine> virtualMachinesSaved = new HashSet<>();
		virtualMachineRepository.deleteByEnvironment(environment);
		
		for (VirtualMachine virtualMachine : virtualMachines) {
			virtualMachine.setEnvironment(environment);
			virtualMachinesSaved.add(save(virtualMachine));
		}
		
		return virtualMachinesSaved;
   }
	
	public Set<VirtualMachine> update(Set<VirtualMachine> virtualMachines, Environment environment) throws ApiException {
		Set<VirtualMachine> virtualMachinesUpdated = new HashSet<>();
		virtualMachineRepository.deleteByEnvironment(environment);
		
		for (VirtualMachine virtualMachine : virtualMachines) {
			virtualMachine.setEnvironment(environment);
			virtualMachinesUpdated.add(update(virtualMachine));
		}
		
		return virtualMachinesUpdated;
   }
	
}