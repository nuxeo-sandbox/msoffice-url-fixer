package org.nuxeo.ecm.platform.ui.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.runtime.api.Framework;

public class MsOfficeUrlFilter implements Filter {

	private static final Logger log = LogManager.getLogger(MsOfficeUrlFilter.class);

	private static final String CONTEXT_PATH_PROPERTY = "org.nuxeo.ecm.contextPath";

	private Map<String, String> urlFragmentPatterns = new HashMap<>();
	
	private String newUrl;

	public MsOfficeUrlFilter() {
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		log.debug("MsOfficeUrlFilter initialization");
		String contextRoot = Framework.getProperty(CONTEXT_PATH_PROPERTY);
		var replacementFragment = String.format("%s/ui/#!", contextRoot);
		if (urlFragmentPatterns.isEmpty()) {
		    urlFragmentPatterns.put(String.format("%s/ui/%%23!", contextRoot), replacementFragment);
		    urlFragmentPatterns.put(String.format("%s/ui/#%%21", contextRoot), replacementFragment);
		    urlFragmentPatterns.put(String.format("%s/ui/%%23%%21", contextRoot), replacementFragment);
		}
	}

	@Override
	public void destroy() {
		log.debug("MsOfficeUrlFilter destroy");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String url = req.getRequestURL().toString();
			newUrl = null;
			urlFragmentPatterns.forEach((pattern, replacement) -> {
			    if (newUrl == null) {
			        log.debug("<doFilter> {} == {} == {}", url, pattern, replacement);
			        if (url.indexOf(pattern) != -1) {
			            newUrl = url.replaceFirst(pattern, replacement);
			            log.debug("<doFilter> Redirection to {}", newUrl);
			        }
			    }
			});
			if (newUrl != null) {
			    HttpServletResponse httpResponse = (HttpServletResponse) response;
			    httpResponse.sendRedirect(newUrl);
			    return;
			}
		}
		chain.doFilter(request, response);
		return;
	}
}
