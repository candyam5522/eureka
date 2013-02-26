/*
 * #%L
 * Eureka Services
 * %%
 * Copyright (C) 2012 - 2013 Emory University
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
package edu.emory.cci.aiw.cvrg.eureka.services.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.inject.Inject;

import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.ClientException;
import edu.emory.cci.aiw.cvrg.eureka.common.comm.clients.EtlClient;
import edu.emory.cci.aiw.cvrg.eureka.common.entity.User;
import edu.emory.cci.aiw.cvrg.eureka.common.exception.HttpStatusException;
import edu.emory.cci.aiw.cvrg.eureka.services.config.ServiceProperties;
import edu.emory.cci.aiw.cvrg.eureka.services.dao.UserDao;

/**
 * A REST resource to allow an administrator to test an account.
 *
 * @author hrathod
 */
@Path("/ping")
@Produces(MediaType.APPLICATION_JSON)
public class PingResource {

	private final UserDao userDao;
	private final EtlClient etlClient;

	@Inject
	public PingResource(UserDao inUserDao, ServiceProperties inProperties) {
		this.userDao = inUserDao;
		this.etlClient = new EtlClient(inProperties.getEtlUrl());
	}

	/**
	 * Test the given account for errors.
	 */
	@GET
	@Path("/{id}")
	public String ping (@PathParam("id") Long inUserId) {
		String response = "Ping successful";
		User user = this.userDao.retrieve(inUserId);
		if (user == null) {
			response = "No user with id " + inUserId;
		} else {
			this.userDao.refresh(user);
			try {
				testUserRoles(user);
				testUserConfiguration(user);
			} catch (ClientException e) {
				throw new HttpStatusException(Response.Status
						.PRECONDITION_FAILED, e.getMessage());
			}
		}
		return response;
	}

	private void testUserConfiguration (User inUser) throws ClientException {
		this.etlClient.ping(inUser.getId());
	}

	private void testUserRoles (User inUser) {
			if (inUser.getRoles() == null || inUser.getRoles().size() < 1) {
				throw new HttpStatusException(Response.Status
						.PRECONDITION_FAILED, "No roles assigned for user " +
						inUser.getId());
			}
	}
}