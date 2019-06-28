package com.uff.phenomanager.domain;

public enum VpnType {
	
	VPN("VPN"), 
	VPNC("Cisco VPN");
	
	private final String vpnDescription;

	VpnType(String vpnDescription) {
		this.vpnDescription = vpnDescription;
	}
	
	public String getVpnDescription() {
		return vpnDescription;
	}
	
	public static VpnType getVpnTypeFromString(String vpn) {
		for (VpnType vpnType : VpnType.values()) {
			if (vpnType.name().equals(vpn) || vpnType.getVpnDescription().equals(vpn)) {
				return vpnType;
			}
		}
		
		return null;
	}
	
}