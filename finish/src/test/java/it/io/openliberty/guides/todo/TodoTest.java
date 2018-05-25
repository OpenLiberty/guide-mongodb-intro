package it.io.openliberty.guides.todo;

import static org.junit.Assert.assertEquals;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

public class TodoTest {
    @Test
    public void testGetTodos() throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:9080/todo-app/todos");
        HttpResponse response = client.execute(request);

        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }
}
