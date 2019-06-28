package com.uff.phenomanager.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uff.phenomanager.domain.ExecutionEnvironment;
import com.uff.phenomanager.domain.VirtualMachineConfig;
import com.uff.phenomanager.exception.ApiException;
import com.uff.phenomanager.repository.VirtualMachineConfigRepository;
import com.uff.phenomanager.service.core.ApiRestService;

@Service
public class VirtualMachineConfigService extends ApiRestService<VirtualMachineConfig, VirtualMachineConfigRepository> {
	
	@Autowired
	private VirtualMachineConfigRepository virtualMachineConfigRepository;
	
	@Override
	protected VirtualMachineConfigRepository getRepository() {
		return virtualMachineConfigRepository;
	}
	
	@Override
	protected Class<VirtualMachineConfig> getEntityClass() {
		return VirtualMachineConfig.class;
	}
	
	public Set<VirtualMachineConfig> save(Set<VirtualMachineConfig> virtualMachineConfigs, ExecutionEnvironment executionEnvironment) throws ApiException {
		Set<VirtualMachineConfig> virtualMachineConfigsSaved = new HashSet<>();
		virtualMachineConfigRepository.deleteByExecutionEnvironment(executionEnvironment);
		
		for (VirtualMachineConfig virtualMachineConfig : virtualMachineConfigs) {
			virtualMachineConfig.setExecutionEnvironment(executionEnvironment);
			virtualMachineConfigsSaved.add(save(virtualMachineConfig));
		}
		
		return virtualMachineConfigsSaved;
   }
	
	public Set<VirtualMachineConfig> update(Set<VirtualMachineConfig> virtualMachineConfigs, ExecutionEnvironment executionEnvironment) throws ApiException {
		Set<VirtualMachineConfig> virtualMachineConfigsUpdated = new HashSet<>();
		virtualMachineConfigRepository.deleteByExecutionEnvironment(executionEnvironment);
		
		for (VirtualMachineConfig virtualMachineConfig : virtualMachineConfigs) {
			virtualMachineConfig.setExecutionEnvironment(executionEnvironment);
			virtualMachineConfigsUpdated.add(update(virtualMachineConfig));
		}
		
		return virtualMachineConfigsUpdated;
   }
	
}