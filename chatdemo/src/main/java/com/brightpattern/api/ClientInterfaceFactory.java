package com.brightpattern.api;

import com.brightpattern.api.data.ConnectionConfig;


public class ClientInterfaceFactory {
	
	public static ClientInterface create(ConnectionConfig cfg) {
		ApiWrapper api = new ApiWrapper(cfg.getServerAddress(), cfg.getTenantUrl(), cfg.getAppId(), cfg.getClientId());
		return new ClientInterfaceImpl(api);
	}
}
