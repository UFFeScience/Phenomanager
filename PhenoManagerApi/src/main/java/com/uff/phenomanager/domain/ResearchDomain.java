package com.uff.phenomanager.domain;

public enum ResearchDomain {
	
	MATH("Math"), 
	PHYSICS("Phisics"),
	BIOLOGY("Biology"),
	CHEMISTRY("Chemistry"),
	BIOCHEMISTRY("Biochemistry"),
	ASTRONOMY("Astronomy"),
	COMPUTER_SCIENCE("Computer Science"),
	LINGUISTICS("Linguistics"),
	OTHER("Other");
	
	private final String domainName;

	ResearchDomain(String domainName) {
		this.domainName = domainName;
	}
	
	public String getDomainName() {
		return domainName;
	}
	
	public static ResearchDomain getResearchDomainFromString(String domainName) {
		for (ResearchDomain role : ResearchDomain.values()) {
			if (role.name().equals(domainName) || role.getDomainName().equals(domainName)) {
				return role;
			}
		}
		
		return null;
	}
	
}