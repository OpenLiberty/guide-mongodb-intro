package io.openliberty.guides.todo.services.samples;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import io.openliberty.guides.todo.models.TodoModel;
import io.openliberty.guides.todo.services.TodoService;

@Alternative
@ApplicationScoped
public class SampleTodoService implements TodoService {
    private List<TodoModel> todos;
    private static int id = 0;

    public SampleTodoService() {
        this.todos = new ArrayList<TodoModel>();
        createTodo(new TodoModel("My first task", false));
        createTodo(new TodoModel("My second task", true));
        createTodo(new TodoModel("My third task", false));
    }

	@Override
	public List<TodoModel> getTodos() {
		return todos;
	}

	@Override
	public TodoModel createTodo(TodoModel todo) {
        TodoModel tm = todo.withId(++id);
        todos.add(tm);
        return tm;
	}

	@Override
	public Optional<TodoModel> findTodo(Integer id) {
        return todos
            .stream()
            .filter(todo -> todo.hasId() && todo.getId() == id)
            .findFirst();
	}

	@Override
	public Optional<TodoModel> updateTodo(Integer id, TodoModel updated) {
        Optional<TodoModel> todo = findTodo(id);
        
        if (todo.isPresent()) {
            TodoModel tm = todo.get();
            tm.setTitle(updated.getTitle());
            tm.setCompleted(updated.getCompleted());
        }

        return todo;
	}

	@Override
	public Optional<TodoModel> deleteTodo(Integer id) {
        Optional<TodoModel> todo = findTodo(id);
        
        if (todo.isPresent()) {
            todos.remove(todo.get());
        }

        return todo;
	}
}
