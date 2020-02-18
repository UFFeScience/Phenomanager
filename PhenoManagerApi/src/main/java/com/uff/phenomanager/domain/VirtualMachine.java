package com.uff.phenomanager.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.uff.phenomanager.domain.core.BaseApiEntity;

@Entity
@Table(name = "virtual_machine")
public class VirtualMachine extends BaseApiEntity {
	
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
	@JoinColumn(name = "id_environment")
	@JsonBackReference(value = "environmentVirtualMachines")
	private Environment environment;
	
	public VirtualMachine() {}
	
	public VirtualMachine(VirtualMachineBuilder builder) {
		this.type = builder.type;
		this.financialCost = builder.financialCost;
		this.diskSpace = builder.diskSpace;
		this.ram = builder.ram;
		this.gflops = builder.gflops;
		this.platform = builder.platform;
		this.numberOfCores = builder.numberOfCores;
		this.environment = builder.environment;
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

	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public static VirtualMachineBuilder builder() {
		return new VirtualMachineBuilder();
	}
	
	public static class VirtualMachineBuilder extends BaseApiEntityBuilder {
		
		private String type;
		private Double financialCost;
		private Double diskSpace;
		private Integer ram;
		private Double gflops;
		private String platform;
		private Integer numberOfCores;
		private Environment environment;
		
		public VirtualMachineBuilder environment(Environment environment) {
			this.environment = environment;
			return this;
		}
		
		public VirtualMachineBuilder type(String type) {
			this.type = type;
			return this;
		}
		
		public VirtualMachineBuilder financialCost(Double financialCost) {
			this.financialCost = financialCost;
			return this;
		}
		
		public VirtualMachineBuilder diskSpace(Double diskSpace) {
			this.diskSpace = diskSpace;
			return this;
		}
		
		public VirtualMachineBuilder ram(Integer ram) {
			this.ram = ram;
			return this;
		}
		
		public VirtualMachineBuilder gflops(Double gflops) {
			this.gflops = gflops;
			return this;
		}
		
		public VirtualMachineBuilder platform(String platform) {
			this.platform = platform;
			return this;
		}
		
		public VirtualMachineBuilder numberOfCores(Integer numberOfCores) {
			this.numberOfCores = numberOfCores;
			return this;
		}
		
		public VirtualMachine build() {
			return new VirtualMachine(this);
		}
	}

}