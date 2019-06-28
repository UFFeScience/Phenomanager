package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "virtual_machine_config")
public class VirtualMachineConfig extends BaseApiEntity {
	
	@Column(name = "type", length = 150)
	private String type;
	
	@Column(name = "financial_cost")
	private Double financialCost;
	
	@Column(name = "disk_space")
	private Double diskSpace;
	
	@Column(name = "ram")
	private Integer ram;
	
	@Column(name = "gflops")
	private Double gflops;
	
	@Column(name = "platform", length = 150)
	private String platform;
	
	@Column(name = "number_of_cores")
	private Integer numberOfCores;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_execution_environment")
	@JsonBackReference(value = "executionEnvironmentVirtualMachineConfigs")
	private ExecutionEnvironment executionEnvironment;
	
	public VirtualMachineConfig() {}
	
	public VirtualMachineConfig(VirtualMachineConfigBuilder builder) {
		this.type = builder.type;
		this.financialCost = builder.financialCost;
		this.diskSpace = builder.diskSpace;
		this.ram = builder.ram;
		this.gflops = builder.gflops;
		this.platform = builder.platform;
		this.numberOfCores = builder.numberOfCores;
		this.executionEnvironment = builder.executionEnvironment;
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
		this.setCountDistinct(builder.getCountDistinct());
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getFinancialCost() {
		return financialCost;
	}

	public void setFinancialCost(Double financialCost) {
		this.financialCost = financialCost;
	}

	public Double getDiskSpace() {
		return diskSpace;
	}

	public void setDiskSpace(Double diskSpace) {
		this.diskSpace = diskSpace;
	}

	public Integer getRam() {
		return ram;
	}

	public void setRam(Integer ram) {
		this.ram = ram;
	}

	public Double getGflops() {
		return gflops;
	}

	public void setGflops(Double gflops) {
		this.gflops = gflops;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public Integer getNumberOfCores() {
		return numberOfCores;
	}

	public void setNumberOfCores(Integer numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	
	public ExecutionEnvironment getExecutionEnvironment() {
		return executionEnvironment;
	}

	public void setExecutionEnvironment(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	public static VirtualMachineConfigBuilder builder() {
		return new VirtualMachineConfigBuilder();
	}
	
	public static class VirtualMachineConfigBuilder extends BaseApiEntityBuilder {
		
		private String type;
		private Double financialCost;
		private Double diskSpace;
		private Integer ram;
		private Double gflops;
		private String platform;
		private Integer numberOfCores;
		private ExecutionEnvironment executionEnvironment;
		
		public VirtualMachineConfigBuilder executionEnvironment(ExecutionEnvironment environmentConfig) {
			this.executionEnvironment = environmentConfig;
			return this;
		}
		
		public VirtualMachineConfigBuilder type(String type) {
			this.type = type;
			return this;
		}
		
		public VirtualMachineConfigBuilder financialCost(Double financialCost) {
			this.financialCost = financialCost;
			return this;
		}
		
		public VirtualMachineConfigBuilder diskSpace(Double diskSpace) {
			this.diskSpace = diskSpace;
			return this;
		}
		
		public VirtualMachineConfigBuilder ram(Integer ram) {
			this.ram = ram;
			return this;
		}
		
		public VirtualMachineConfigBuilder gflops(Double gflops) {
			this.gflops = gflops;
			return this;
		}
		
		public VirtualMachineConfigBuilder platform(String platform) {
			this.platform = platform;
			return this;
		}
		
		public VirtualMachineConfigBuilder numberOfCores(Integer numberOfCores) {
			this.numberOfCores = numberOfCores;
			return this;
		}
		
		public VirtualMachineConfig build() {
			return new VirtualMachineConfig(this);
		}
	}

}