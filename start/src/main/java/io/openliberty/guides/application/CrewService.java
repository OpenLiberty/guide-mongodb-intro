/*******************************************************************************
* Copyright (c) 2018 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package io.openliberty.guides.application;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import javax.enterprise.context.ApplicationScoped;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class CrewService {

	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(
			responseCode = "200",
			description = "Successfully added crew member."),
		@APIResponse(
			responseCode = "400",
			description = "Invalid crew member configuration.") })
	@Operation(summary = "Add a new crew member to the database.")
	public Response add(CrewMember crewMember) {
		return Response.ok().build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(
			responseCode = "200",
			description = "Successfully deleted crew member."),
		@APIResponse(
			responseCode = "400",
			description = "Invalid object id."),
		@APIResponse(
			responseCode = "404",
			description = "Crew member object id was not found.") })
	@Operation(summary = "Delete a crew member from the database.")
	public Response remove(
		@Parameter(
			description = "Object id of the crew member to delete.",
			required = true
		)
		@PathParam("id") String id) {
		return Response.ok().build();
	}

	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(
			responseCode = "200",
			description = "Successfully updated crew member."),
		@APIResponse(
			responseCode = "400",
			description = "Invalid object id or crew member configuration."),
		@APIResponse(
			responseCode = "404",
			description = "Crew member object id was not found.") })
	@Operation(summary = "Update a crew member in the database.")
	public Response update(CrewMember crewMember,
		@Parameter(
			   description = "Object id of the crew member to update.",
			   required = true
		)
		@PathParam("id") String id) {
		return Response.ok().build();
	}

	@GET
	@Path("/members")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
		@APIResponse(
			responseCode = "200",
			description = "Successfully listed the crew members."),
		@APIResponse(
			responseCode = "500",
			description = "Failed to list the crew members.") })
	@Operation(summary = "List the crew members from the database.")
	public Response retrieve() {
		return Response.ok().build();
	}
}