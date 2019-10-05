package com.uff.model.invoker.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.uff.model.invoker.Constants;
import com.uff.model.invoker.Constants.MSG_ERROR;
import com.uff.model.invoker.Constants.MSG_WARN;

public class IpAddressValidator {
	
	private static final Logger log = LoggerFactory.getLogger(IpAddressValidator.class);

	@Value(Constants.PROFILE_PROPERTY)
	private String activeProfile;
	
	public Boolean validateWorkspaceAddress(String ip) { 
		if (Constants.DEFAULT_PROFILE.equals(activeProfile) ) {
			return Boolean.TRUE;
		}
		
		if (ip == null || "".equals(ip)) {
			log.warn(MSG_WARN.INVALID_INCOMING_IP);
			return Boolean.FALSE;
		}
		
		return validateLocalAddress(ip) && validateExternalAddress(ip);
    } 
	
	public Boolean validateLocalAddress(String hostAddress) {
		try {
			if (InetAddress.getLocalHost().getHostAddress().trim().equals(hostAddress)) {
				return Boolean.FALSE;
			}
			
			Enumeration<NetworkInterface> netWorkInterfaces = NetworkInterface.getNetworkInterfaces();
			
			while (netWorkInterfaces.hasMoreElements()) {
				NetworkInterface netWorkInterface = netWorkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddress = netWorkInterface.getInetAddresses();
				
				while (inetAddress.hasMoreElements()) {
					InetAddress address = inetAddress.nextElement();
					
					if (address.getHostAddress().trim().equals(hostAddress) || address.getHostName().trim().equals(hostAddress) || 
							address.getCanonicalHostName().trim().equals(hostAddress)) {
						return Boolean.FALSE;
					}
				}
			}
			
		} catch (Exception e) {
			log.error(MSG_ERROR.ERROR_GET_LOCAL_IP, e);
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}
	
	public Boolean validateExternalAddress(String hostAddress) {
		if (Constants.SYSTEM_HOST_ADDRESS_NAME.equals(hostAddress)) {
			return Boolean.FALSE;
		}
		
		try { 
            URL urlName = new URL(Constants.CHECK_IP_FIRST_OPTION); 
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlName.openStream())); 
            String systemipaddress = bufferedReader.readLine().trim(); 
            
            if (systemipaddress == null || "".equals(systemipaddress) || systemipaddress.equals(hostAddress)) {
            	return Boolean.FALSE;
            }
       
        }  catch (Exception e) { 
        	log.error(MSG_ERROR.ERROR_GET_EXTERNAL_IP, Constants.CHECK_IP_FIRST_OPTION, e);
        
        	try {
        		URL urlName = new URL(Constants.CHECK_IP_SECOND_OPTION); 
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlName.openStream())); 
                String systemipaddress = bufferedReader.readLine().trim(); 
                
                if (systemipaddress == null || "".equals(systemipaddress) || systemipaddress.equals(hostAddress)) {
                	return Boolean.FALSE;
                }	
        		
        	} catch (Exception ex) {
        		log.error(MSG_ERROR.ERROR_GET_EXTERNAL_IP, Constants.CHECK_IP_SECOND_OPTION, e);
        		return Boolean.FALSE;
        	}
        }
        
        return Boolean.TRUE;
	}
	
}