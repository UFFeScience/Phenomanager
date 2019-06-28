package com.uff.model.invoker.domain;

public class AmazonMachine {
	
	private String name;
	private String ami;
	private String type;
	private Integer rank;
	private Integer numberOfCores = 1;
	private String publicDNS;
	private String privateIP;
	private String publicIP;
	
	public AmazonMachine() {}
	
	public AmazonMachine(AmazonMachineBuilder amazonMachineBuilder) {
		this.name = amazonMachineBuilder.name;
		this.ami = amazonMachineBuilder.ami;
		this.rank = amazonMachineBuilder.rank;
		this.type = amazonMachineBuilder.type;
		this.numberOfCores = amazonMachineBuilder.numberOfCores;
		this.publicDNS = amazonMachineBuilder.publicDNS;
		this.privateIP = amazonMachineBuilder.privateIP;
		this.publicIP = amazonMachineBuilder.publicIP;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getAmi() {
		return ami;
	}

	public void setAmi(String ami) {
		this.ami = ami;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Integer getNumberOfCores() {
		return numberOfCores;
	}

	public void setNumberOfCores(Integer numberOfCores) {
		this.numberOfCores = numberOfCores;
	}

	public String getPublicDNS() {
		return publicDNS;
	}

	public void setPublicDNS(String publicDNS) {
		this.publicDNS = publicDNS;
	}

	public String getPrivateIP() {
		return privateIP;
	}

	public void setPrivateIP(String privateIP) {
		this.privateIP = privateIP;
	}

	public String getPublicIP() {
		return publicIP;
	}

	public void setPublicIP(String publicIP) {
		this.publicIP = publicIP;
	}
	
	public static AmazonMachineBuilder builder() {
		return new AmazonMachineBuilder();
	}
	
	public static class AmazonMachineBuilder {
		
		private String name;
		private String ami;
		private String type;
		private Integer rank;
		private Integer numberOfCores = 1;
		private String publicDNS;
		private String privateIP;
		private String publicIP;
		
		public AmazonMachineBuilder name(String name) {
			this.name = name;
			return this;
		}
		
		public AmazonMachineBuilder ami(String ami) {
			this.ami = ami;
			return this;
		}
		
		public AmazonMachineBuilder type(String type) {
			this.type = type;
			return this;
		}
		
		public AmazonMachineBuilder rank(Integer rank) {
			this.rank = rank;
			return this;
		}
		
		public AmazonMachineBuilder numberOfCores(Integer numberOfCores) {
			this.numberOfCores = numberOfCores;
			return this;
		}
		
		public AmazonMachineBuilder publicDNS(String publicDNS) {
			this.publicDNS = publicDNS;
			return this;
		}
		
		public AmazonMachineBuilder privateIP(String privateIP) {
			this.privateIP = privateIP;
			return this;
		}
		
		public AmazonMachineBuilder publicIP(String publicIP) {
			this.publicIP = publicIP;
			return this;
		}
		
		public AmazonMachine build() {
			return new AmazonMachine(this);
		}
	}
}
