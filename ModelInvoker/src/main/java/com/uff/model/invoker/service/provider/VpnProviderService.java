package com.uff.model.invoker.service.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.uff.model.invoker.Constants.PROVIDER;
import com.uff.model.invoker.domain.VpnType;
import com.uff.model.invoker.util.FileUtils;

@Component
public class VpnProviderService {
	
	private static final Logger log = LoggerFactory.getLogger(VpnProviderService.class);

	public Process setupVpnConfigConection(VpnType vpnType, Long fileTag, String vpnConfiguration) throws IOException, FileNotFoundException, Exception {
		Process vpnProcess = null;
		
		if (vpnType != null && vpnConfiguration != null) {
			vpnProcess = openVpnConnection(vpnType, FileUtils.writeStringToFile(PROVIDER.VPN.CONFIG_FILE_PREFIX + fileTag, 
					PROVIDER.VPN.CONFIG_FILE_SUFFIX, vpnConfiguration));
		}
		
		return vpnProcess;
	}

	public Process openVpnConnection(VpnType vpnType, File vpnConfigurationFile) throws Exception {
		log.info("Openning VPN conection of type [{}]", vpnType);
		
		StringBuilder commandLine = new StringBuilder(VpnType.VPN.equals(vpnType) ? PROVIDER.VPN.VPN_COMMAND : PROVIDER.VPN.VPNC_COMMAND)
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
			log.error("Error while closing VPN conection", e);
			return Boolean.FALSE;
		}
	}

}