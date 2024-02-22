package org.nuxeo.ecm.platform.ui.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private static final String CUSTOM_WEBUI_CONTEXT_PATH_PROPERTY = "msofficeurlfixer.webui.contextPath";

	private Map<String, String> urlFragmentPatterns = new HashMap<>();
	
	private List<String> webUiUrlFragmentPatterns = Stream.of("ui").collect(Collectors.toList());;
	
	public MsOfficeUrlFilter() {
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		log.debug("MsOfficeUrlFilter initialization");
        if (urlFragmentPatterns.isEmpty()) {
            String contextRoot = Framework.getProperty(CONTEXT_PATH_PROPERTY);
            String webuiContextRoot = Framework.getProperty(CUSTOM_WEBUI_CONTEXT_PATH_PROPERTY);
            if (webuiContextRoot != null) {
                for (var uiPattern : Arrays.stream(webuiContextRoot.split(",")).map(String::trim).collect(Collectors.toList())) {
                    log.debug("<init> UI url: {}", uiPattern);
                    webUiUrlFragmentPatterns.add(uiPattern);
                }
            }
            for (var uiPattern : webUiUrlFragmentPatterns) {
                var replacementFragment = String.format("%s/%s/#!", contextRoot, uiPattern);
                urlFragmentPatterns.put(String.format("%s/%s/%%23!", contextRoot, uiPattern), replacementFragment);
                urlFragmentPatterns.put(String.format("%s/%s/#%%21", contextRoot, uiPattern), replacementFragment);
                urlFragmentPatterns.put(String.format("%s/%s/%%23%%21", contextRoot, uiPattern), replacementFragment);
            }
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
			for (var entry : urlFragmentPatterns.entrySet()) {
			    var pattern = entry.getKey();
			    var replacement = entry.getValue();
                log.debug("<doFilter> {} == {} == {}", url, pattern, replacement);
                if (url.indexOf(pattern) != -1) {
                    var newUrl = url.replaceFirst(pattern, replacement);
                    log.debug("<doFilter> Redirection to {}", newUrl);
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    httpResponse.sendRedirect(newUrl);
                    return;
                }
			}
		}
		chain.doFilter(request, response);
		return;
	}
}
