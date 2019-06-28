package com.uff.phenomanager.domain;

public enum Role {
	
	ADMIN("Admin"), 
	USER("User");
	
	private final String roleName;

	Role(String roleName) {
		this.roleName = roleName;
	}
	
	public String getRoleName() {
		return roleName;
	}
	
	public static Role getRoleFromString(String userRole) {
		for (Role role : Role.values()) {
			if (role.name().equals(userRole) || role.getRoleName().equals(userRole)) {
				return role;
			}
		}
		
		return null;
	}
	
}