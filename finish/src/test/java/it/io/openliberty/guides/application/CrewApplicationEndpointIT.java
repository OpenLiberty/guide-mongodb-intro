//tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
// end::copyright[]
package it.io.openliberty.guides.application;

import static org.junit.Assert.assertEquals;

import javax.json.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.ArrayList;

public class CrewApplicationEndpointIT {

    private Client _Client;
    private JsonArray _TestData;
    private String _RootURL;
    private ArrayList<String> _TestIDs = new ArrayList<>(2);

    // tag::Before[]
    @Before
    // end::Before[]
    public void setup() {
        _Client = ClientBuilder.newClient();
        _Client.register(JsrJsonpProvider.class);

        String port = System.getProperty("app.http.port");
        String context = System.getProperty("app.context.root");
        _RootURL = "http://localhost:" + port + context;

        // test data
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("name", "Member1");
        jsonBuilder.add("crewID", "000001");
        jsonBuilder.add("rank", "Captain");
        arrayBuilder.add(jsonBuilder.build());
        jsonBuilder = Json.createObjectBuilder();
        jsonBuilder.add("name", "Member2");
        jsonBuilder.add("crewID", "000002");
        jsonBuilder.add("rank", "Engineer");
        arrayBuilder.add(jsonBuilder.build());
        _TestData = arrayBuilder.build();
    }

    // tag::After[]
    @After
    // end::After[]
    // tag::teardown[]
    public void teardown() {
        _Client.close();
    }
    // end::teardown[]

    // tag::test[]
    @Test
    // end::test[]
    public void testSuite() {
        this.testAddCrewMember();
        this.testUpdateCrewMember();
        this.testGetCrewMembers();
        this.testDeleteCrewMember();
    }

    // tag::testAddCrewMember[]
    private void testAddCrewMember() {
        System.out.println("   === Adding " + _TestData.size() + " testing crew members to the database. ===");
        for (int i = 0; i < _TestData.size(); i ++) {
            JsonObject member = (JsonObject) _TestData.get(i);
            String url = _RootURL + "/crew" + "/" + member.getString("crewID");
            Response response = _Client.target(url).request().post(Entity.json(member));
            this.assertResponse(url, response);

            JsonObject newMember = response.readEntity(JsonObject.class);
            _TestIDs.add(newMember.getJsonObject("_id").getString("$oid"));

            response.close();
        }
        System.out.println("      === Done. ===");
    }
    // end::testAddCrewMember[]

    // tag::testUpdateCrewMember[]
    public void testUpdateCrewMember() {
        System.out.println("   === Updating crew member with id " + _TestIDs.get(0) + ". ===");

        JsonObject oldMember = (JsonObject) _TestData.get(0);

        JsonObjectBuilder newMember = Json.createObjectBuilder();
        newMember.add("name", oldMember.get("name"));
        newMember.add("crewID", oldMember.get("crewID"));
        newMember.add("rank", "Officer");

        String url = _RootURL + "/crew" + "/" + _TestIDs.get(0);
        Response response = _Client.target(url).request().put(Entity.json(newMember.build()));

        this.assertResponse(url, response);

        System.out.println("      === Done. ===");
    }
    // end::testUpdateCrewMember[]

    // tag::testGetCrewMembers[]
    private void testGetCrewMembers() {
        System.out.println("   === Listing crew members from the database. ===");

        String url = _RootURL + "/crew";
        Response response = _Client.target(url).request().get();

        this.assertResponse(url, response);

        String responseText = response.readEntity(String.class);
        JsonReader reader = Json.createReader(new StringReader(responseText));
        JsonArray crew = reader.readArray();
        reader.close();

        int testMemberCount = 0;
        for (JsonValue value : crew) {
            JsonObject member = (JsonObject) value;
            String id = member.getJsonObject("_id").getString("$oid");
            if (_TestIDs.contains(id)) {
                testMemberCount ++;
            }
        }
        assertEquals("Incorrect number of testing members: ", _TestIDs.size(), testMemberCount);

        System.out.println("      === Done. There are " + crew.size() + " members. ===");

        response.close();
    }
    // end::testGetCrewMembers[]

    // tag::testDeleteCrewMember[]
    private void testDeleteCrewMember() {
        System.out.println("   === Removing " + _TestIDs.size() + " testing crew members from the database. ===");
        for (String id : _TestIDs) {
            String url = _RootURL + "/crew" + "/" + id;
            Response response = _Client.target(url).request().delete();
            this.assertResponse(url, response);
            response.close();
        }
        System.out.println("      === Done. ===");
    }
    // end::testDeleteCrewMember[]

    /**
     * <p>
     * Asserts that the given URL has the correct response code of 200.
     * </p>
     *
     * @param url
     *          - target URL.
     * @param response
     *          - response received from the target URL.
     */
    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());
    }
}