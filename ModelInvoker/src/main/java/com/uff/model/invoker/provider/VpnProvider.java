package com.uff.model.invoker.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.domain.VpnType;
import com.uff.model.invoker.util.FileUtils;

@Component
public class VpnProvider {
	
	private static final Logger log = LoggerFactory.getLogger(VpnProvider.class);

	public static final String VPNC_CONFIG_FILE_SUFIX = ".conf";
	private static final String VPN_COMMAND = "sudo openvpn ";
	private static final String VPNC_COMMAND = "sudo vpnc ";
	
	public Process setupVpnConfigConection(VpnType vpnType, Long fileTag, String vpnConfiguration) throws IOException, FileNotFoundException, Exception {
		Process vpnProcess = null;
		
		if (vpnType != null && vpnConfiguration != null) {
			vpnProcess = openVpnConnection(vpnType, FileUtils.writeStringToFile("vpn-config-" + fileTag, 
					VPNC_CONFIG_FILE_SUFIX, vpnConfiguration));
		}
		
		return vpnProcess;
	}

	public Process openVpnConnection(VpnType vpnType, File vpnConfigurationFile) throws Exception {
		log.info("Openning VPN conection of type [{}]", vpnType);
		
		StringBuilder commandLine = new StringBuilder(VpnType.VPN.equals(vpnType) ? VPN_COMMAND : VPNC_COMMAND)
			       .append(vpnConfigurationFile.getName());

		Process vpnProcess = Runtime.getRuntime().exec(commandLine.toString());

		if (vpnProcess.exitValue() != 0) {
			log.warn("Failed to initialize VPN process");
			return null;
		
		} else {
			return vpnProcess;
		}
	}
	
	public Boolean closeVpnConnection(Process vpnProcess) {
		log.info("Closing VPN conection");
		
		if (vpnProcess == null) {
			return Boolean.TRUE;
		}
		
		try {
			vpnProcess.destroyForcibly();
			return Boolean.TRUE;
			
		} catch (Exception e) {
			log.info("Error while closing VPN conection");
			return Boolean.FALSE;
		}
	}

}