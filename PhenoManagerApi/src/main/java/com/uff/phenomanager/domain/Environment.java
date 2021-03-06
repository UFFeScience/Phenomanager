package com.uff.phenomanager.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.uff.phenomanager.domain.core.BaseApiEntity;

@Entity
@Table(name = "environment")
public class Environment extends BaseApiEntity {
	
	@Column(name = "tag", length = 80, nullable = false)
	private String tag;
	
	@Column(name = "host_address", columnDefinition = "text")
	private String hostAddress;
	
	@Column(name = "username", length = 150)
	private String username;
	
	@Column(name = "password", length = 150)
	private String password;
	
	@Column(name = "cluster_name", length = 150)
	private String clusterName;
	
	@Column(name = "secret_key", length = 150)
	private String secretKey;
	
	@Column(name = "access_key", length = 150)
	private String accessKey;
	
	@Column(name = "image", columnDefinition = "text")
	private String image;
	
	@Column(name = "vpn_type")
	@Enumerated(EnumType.STRING)
	private VpnType vpnType;
	
	@Column(name = "vpn_configuration", columnDefinition = "text")
	private String vpnConfiguration;
    
	@OneToMany(mappedBy = "environment")
	@Cascade(CascadeType.DELETE)
	@JsonManagedReference(value = "environmentVirtualMachines")
	private Set<VirtualMachine> virtualMachines;
	
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private EnvironmentType type;
	
	@ManyToOne
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "id_computational_model")
	private ComputationalModel computationalModel;
	
	public Environment() {}
	
	public Environment(EnvironmentBuilder builder) {
		this.tag = builder.tag;
		this.computationalModel = builder.computationalModel;
		this.type = builder.type;
		this.hostAddress = builder.hostAddress;
		this.username = builder.username;
		this.password = builder.password;
		this.clusterName = builder.clusterName;
		this.secretKey = builder.secretKey;
		this.accessKey = builder.accessKey;
		this.image = builder.image;
		this.virtualMachines = builder.virtualMachines;
		this.vpnType = builder.vpnType;
		this.vpnConfiguration = builder.vpnConfiguration;
		this.setId(builder.getId());
		this.setSlug(builder.getSlug());
		this.setActive(builder.getActive());
		this.setInsertDate(builder.getInsertDate());
		this.setUpdateDate(builder.getUpdateDate());
		this.setDeleteDate(builder.getRemoveDate());
		this.setSum(builder.getSum());
		this.setAvg(builder.getAvg());
		this.setCount(builder.getCount());
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getHostAddress() {
		return hostAddress;
	}

	public void setHostAddress(String hostAddress) {
		this.hostAddress = hostAddress.trim();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password.trim();
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey.trim();
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey.trim();
	}
	
	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName.trim();
	}
	
	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Set<VirtualMachine> getVirtualMachines() {
		if (virtualMachines == null) {
			virtualMachines = new HashSet<>();
		}
		
		return virtualMachines;
	}

	public void setVirtualMachines(Set<VirtualMachine> virtualMachines) {
		this.virtualMachines = virtualMachines;
	}
	
	public VpnType getVpnType() {
		return vpnType;
	}

	public void setVpnType(VpnType vpnType) {
		this.vpnType = vpnType;
	}

	public String getVpnConfiguration() {
		return vpnConfiguration;
	}

	public void setVpnConfiguration(String vpnConfiguration) {
		this.vpnConfiguration = vpnConfiguration;
	}
	
	public EnvironmentType getType() {
		return type;
	}

	public void setType(EnvironmentType type) {
		this.type = type;
	}

	public ComputationalModel getComputationalModel() {
		return computationalModel;
	}

	public void setComputationalModel(ComputationalModel computationalModel) {
		this.computationalModel = computationalModel;
	}
	
	public static EnvironmentBuilder builder() {
		return new EnvironmentBuilder();
	}
	
	public static class EnvironmentBuilder extends BaseApiEntityBuilder {
		
		private String tag;
		private String hostAddress;
		private String username;
		private String password;
		private String secretKey;
		private String accessKey;
		private String clusterName;
		private String image;
		private Set<VirtualMachine> virtualMachines;
		private VpnType vpnType;
		private String vpnConfiguration;
		private ComputationalModel computationalModel;
		private EnvironmentType type;
		
		public EnvironmentBuilder tag(String tag) {
			this.tag = tag;
			return this;
		}
		
		public EnvironmentBuilder vpnType(VpnType vpnType) {
			this.vpnType = vpnType;
			return this;
		}
		
		public EnvironmentBuilder vpnConfiguration(String vpnConfiguration) {
			this.vpnConfiguration = vpnConfiguration;
			return this;
		}
		
		public EnvironmentBuilder image(String image) {
			this.image = image;
			return this;
		}
		
		public EnvironmentBuilder virtualMachines(Set<VirtualMachine> virtualMachines) {
			this.virtualMachines = virtualMachines;
			return this;
		}
		
		public EnvironmentBuilder clusterName(String clusterName) {
			this.clusterName = clusterName;
			return this;
		}
		
		public EnvironmentBuilder hostAddress(String hostAddress) {
			this.hostAddress = hostAddress;
			return this;
		}
		
		public EnvironmentBuilder username(String username) {
			this.username = username;
			return this;
		}
		
		public EnvironmentBuilder password(String password) {
			this.password = password;
			return this;
		}
		
		public EnvironmentBuilder secretKey(String secretKey) {
			this.secretKey = secretKey;
			return this;
		}
		
		public EnvironmentBuilder accessKey(String accessKey) {
			this.accessKey = accessKey;
			return this;
		}
		
		public EnvironmentBuilder computationalModel(ComputationalModel computationalModel) {
			this.computationalModel = computationalModel;
			return this;
		}
		
		public EnvironmentBuilder type(EnvironmentType type) {
			this.type = type;
			return this;
		}
		
		public Environment build() {
			return new Environment(this);
		}
	}

}