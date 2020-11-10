/*
 * (C) Copyright 2020 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thierry Martins
 */
package org.nuxeo.ecm.platform.ui.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.api.Framework;

public class MsOfficeUrlFilter implements Filter {

	private static final Log log = LogFactory.getLog(MsOfficeUrlFilter.class);

	private static final String CONTEXT_PATH_PROPERTY = "org.nuxeo.ecm.contextPath";

	private String fragmentToMatch;

	private String replacementFragment;

	public MsOfficeUrlFilter() {
	}

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		log.debug("MsOfficeUrlFilter initialization");
		String contextRoot = Framework.getProperty(CONTEXT_PATH_PROPERTY);
		fragmentToMatch = String.format("%s/ui/%%23!", contextRoot);
		replacementFragment = String.format("%s/ui/#!", contextRoot);
		return;
	}

	@Override
	public void destroy() {
		log.debug("MsOfficeUrlFilter destroy");
		return;
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String url = req.getRequestURL().toString();
			if (url.indexOf(fragmentToMatch) != -1) {
				String newUrl = url.replaceFirst(fragmentToMatch, replacementFragment);
				log.debug("Redirection to " + newUrl);
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				httpResponse.sendRedirect(newUrl);
				return;
			}
		}
		chain.doFilter(request, response);
		return;
	}
}
