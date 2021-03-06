package edu.emory.cci.aiw.cvrg.eureka.servlet.filter;

/*-
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 - 2016 Emory University
 * %%
 * This program is dual licensed under the Apache 2 and GPLv3 licenses.
 * 
 * Apache License, Version 2.0:
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * GNU General Public License version 3:
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import com.google.inject.Inject;
import org.eurekaclinical.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import edu.emory.cci.aiw.cvrg.eureka.webapp.config.RequestAttributes;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.eurekaclinical.common.comm.Role;
import org.eurekaclinical.standardapis.filter.RolesRequestWrapper;
import org.eurekaclinical.user.client.comm.User;

/**
 *
 * @author Andrew Post
 */
@Singleton
public class RolesFilter implements Filter {

	private final ServicesClient servicesClient;

	@Inject
	public RolesFilter(ServicesClient inClient) {
		this.servicesClient = inClient;
	}

	@Override
	public void init(FilterConfig fc) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest inRequest, ServletResponse inResponse, FilterChain inChain) throws IOException, ServletException {
		HttpServletRequest servletRequest = (HttpServletRequest) inRequest;
		User user = (User) servletRequest.getAttribute(RequestAttributes.USER);//set up eureka_main.jsp user value
		if (user != null) {
			try {
				HttpSession session = servletRequest.getSession(false);
				assert session != null : "session should not be null";

				Principal principal = servletRequest.getUserPrincipal();
				assert principal != null : "principal should not be null";

				Set<Long> userRoleIds = new HashSet<>(user.getRoles());//user project user'role info
                                
				List<Role> roles = this.servicesClient.getRoles();//eureka project roles table
                                
				Map<Long, Role> idsToRoles = new HashMap<>();
                                
				for (Role role : roles) {
					if (userRoleIds.contains(role.getId())) {
						idsToRoles.put(role.getId(), role);
					}
				}
                                
				Set<String> roleNames = new HashSet<>();
				for (Map.Entry<Long, Role> idToRole : idsToRoles.entrySet()) {
					roleNames.add(idToRole.getValue().getName());
				}
				HttpServletRequest wrappedRequest = new RolesRequestWrapper(
						servletRequest, principal, roleNames);

				inChain.doFilter(wrappedRequest, inResponse);
			} catch (ClientException ce) {
				throw new ServletException(ce);
			}
		} else {
			inChain.doFilter(inRequest, inResponse);
		}

	}

	@Override
	public void destroy() {
	}

}
