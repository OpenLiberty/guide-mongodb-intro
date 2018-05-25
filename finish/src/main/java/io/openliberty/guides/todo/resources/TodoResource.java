package io.openliberty.guides.todo.resources;

import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.openliberty.guides.todo.models.TodoModel;
import io.openliberty.guides.todo.services.TodoService;

@Path("todos")
public class TodoResource {
    @Inject
    private TodoService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodos() {
        return Response.ok(service.getTodos()).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response getTodo(@PathParam("id") int id) {
        Optional<TodoModel> result = service.findTodo(id);
        if (!result.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTodo(TodoModel todo) {
        if (todo == null || todo.hasId()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        return Response.ok(service.createTodo(todo)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response updateTodo(@PathParam("id") int id, TodoModel updated) {
        if (updated == null || updated.hasId()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Optional<TodoModel> result = service.updateTodo(id, updated);
        if (!result.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Response deleteTodo(@PathParam("id") int id) {
        Optional<TodoModel> result = service.deleteTodo(id);
        if (!result.isPresent()) {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.ok(result.get()).build();
    }
}
