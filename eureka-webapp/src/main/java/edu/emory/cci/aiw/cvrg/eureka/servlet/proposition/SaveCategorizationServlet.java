/*
 * #%L
 * Eureka WebApp
 * %%
 * Copyright (C) 2012 Emory University
 * %%
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
 * #L%
 */
package edu.emory.cci.aiw.cvrg.eureka.servlet.proposition;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.CategoricalElement;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ServicesClient;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.User;

public class SaveCategorizationServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	        throws ServletException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		CategoricalElement element = objectMapper.readValue(req.getReader(),
		        CategoricalElement.class);

		String username = req.getUserPrincipal().getName();
		String servicesUrl = req.getSession().getServletContext().getInitParameter("eureka-services-url");
		ServicesClient servicesClient = new ServicesClient(servicesUrl);
		User user = servicesClient.getUserByName(username);
		element.setUserId(user.getId());

		try {
			servicesClient.saveCategoricalElement(element);
		} catch (ClientException e) {
			req.setAttribute("error", e.getMessage());
		}
	}

}
