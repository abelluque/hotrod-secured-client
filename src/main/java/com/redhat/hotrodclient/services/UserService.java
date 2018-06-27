package com.redhat.hotrodclient.services;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.infinispan.protostream.DescriptorParserException;

import com.redhat.hotrodclient.model.User;

public interface UserService {

	@POST
	@Path("/createUser")
	@Consumes("application/json")
	public void createUser(User user);

	@GET
	@Path("/getUser/{userName}")
	@Consumes("application/json")
	@Produces("application/json")
	public User getUser(@PathParam("userName") String userName);
	

}
