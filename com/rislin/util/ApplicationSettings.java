package com.rislin.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class ApplicationSettings extends HttpServlet {

	private static String campaignApiUrl;

	public static String getCampaignApiUrl() {
		return campaignApiUrl;
	}

	public static void setCampaignApiUrl(String campaignApiUrl) {
		ApplicationSettings.campaignApiUrl = campaignApiUrl;
	}
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = config.getServletContext();
		campaignApiUrl = context.getInitParameter("campaign.api.url");
	}
	
}
