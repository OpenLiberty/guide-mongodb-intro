// tag::copyright[]
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
// end::copyright[]
package io.openliberty.guides.application;

import java.util.Set;

import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.Json;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import javax.validation.Validator;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/")
@ApplicationScoped
public class CrewService {

	// tag::dbInjection[]
	@Inject
	MongoDatabase db;
	// end::dbInjection[]

	// tag::beanValidator[]
	@Inject
	Validator validator;
	// end::beanValidator[]

	// tag::create[]
	@POST
	@Path("/add")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
			@APIResponse(
					responseCode = "200",
					description = "Crew member successfully added to the database."),
			@APIResponse(
					responseCode = "400",
					description = "Invalid crew member configuration.") })
    @Operation(summary = "Add a new crew member.")
	public Response add(CrewMember crewMember) {

		Set<ConstraintViolation<CrewMember>> violations = validator.validate(
				crewMember
		);

		if(violations.size() > 0) {
			JsonArrayBuilder messages = Json.createArrayBuilder();
			for (ConstraintViolation<CrewMember> v : violations) {
				messages.add(v.getMessage());
			}
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity(messages.build().toString())
					.build();
		}

		MongoCollection<Document> crew = db.getCollection("Crew");

		Document newCrewMember = new Document();
		newCrewMember.put("Name", crewMember.getName());
		newCrewMember.put("Rank", crewMember.getRank());
		newCrewMember.put("CrewID", crewMember.getCrewID());

		crew.insertOne(newCrewMember);

		return Response
				.status(Response.Status.OK)
				.entity(newCrewMember.toJson())
				.build();
	}
	// end::create[]

	// tag::delete[]
	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
			@APIResponse(
					responseCode = "200",
					description = "Crew member successfully removed from the database."),
			@APIResponse(
					responseCode = "400",
					description = "Invalid object id."),
			@APIResponse(
					responseCode = "404",
					description = "Crew member object id was not found.") })
    @Operation(summary = "Delete a crew member.")
	public Response remove(
			@Parameter(
					description = "Object id of the crew member to delete.",
					required = true
			)
			@PathParam("id") String id) {
		MongoCollection<Document> crew = db.getCollection("Crew");

		Document docId;

		try {
			docId = new Document("_id", new ObjectId(id));
		} catch (Exception e) {
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("[\"Invalid object id!\"]")
					.build();
		}

		DeleteResult deleteResult = crew.deleteOne(docId);
		
		if (deleteResult.getDeletedCount() == 0) {
			return Response
					.status(Response.Status.NOT_FOUND)
					.entity("[\"_id was not found!\"]")
					.build();
		}

		return Response
				.status(Response.Status.OK)
				.entity(docId.toJson())
				.build();
	}
	// end::delete[]

	// tag::update[]
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
			@APIResponse(
					responseCode = "200",
					description = "Crew member successfully updated in the database."),
			@APIResponse(
					responseCode = "400",
					description = "Invalid object id or crew member configuration."),
			@APIResponse(
					responseCode = "404",
					description = "Crew member object id was not found.") })
    @Operation(summary = "Update a crew member.")
	public Response update(CrewMember crewMember,
			@Parameter(
					description = "Object id of the crew member to update.",
					required = true
			)
			@PathParam("id") String id) {

		Set<ConstraintViolation<CrewMember>> violations = validator.validate(
				crewMember
		);

		if(violations.size() > 0) {
			JsonArrayBuilder messages = Json.createArrayBuilder();
			for (ConstraintViolation<CrewMember> v : violations) {
				messages.add(v.getMessage());
			}
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity(messages.build().toString())
					.build();
		}

		MongoCollection<Document> crew = db.getCollection("Crew");

		ObjectId oid;

		try {
			oid = new ObjectId(id);
		} catch (Exception e) {
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("[\"Invalid object id!\"]")
					.build();
		}

		Document query = new Document("_id", oid);

		Document newCrewMember = new Document();
		newCrewMember.put("Name", crewMember.getName());
		newCrewMember.put("Rank", crewMember.getRank());
		newCrewMember.put("CrewID", crewMember.getCrewID());

		UpdateResult updateResult = crew.replaceOne(query , newCrewMember);

		if (updateResult.getMatchedCount() == 0) {
			return Response
					.status(Response.Status.NOT_FOUND)
					.entity("[\"_id was not found!\"]")
					.build();
		}

        newCrewMember.put("_id", oid);

		return Response
				.status(Response.Status.OK)
				.entity(newCrewMember.toJson())
				.build();
	}
	// end::update[]

	// tag::read[]
	// end::read[]
	@GET
	@Path("/members")
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponses({
			@APIResponse(
					responseCode = "200",
					description = "Successfully retrieved crew members from the database."),
			@APIResponse(
					responseCode = "500",
					description = "Failed to retrieve crew members from the database.") })
	@Operation(summary = "List the crew members.")
	public Response retrieve() {
		StringWriter sb = new StringWriter();

		try {
			MongoCollection<Document> crew = db.getCollection("Crew");
			sb.append("[");
			boolean first = true;
			for (Document d : crew.find()) {
				if (!first) sb.append(",");
				else first = false;
				sb.append(d.toJson());
			}
			sb.append("]");
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response
					.serverError()
					.build();
		}

		return Response
				.status(Response.Status.OK)
				.entity(sb.toString())
				.build();
	}
}