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
package io.openliberty.guides.mongo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import com.ibm.websphere.crypto.PasswordUtil;
import com.ibm.websphere.ssl.JSSEHelper;
import com.ibm.websphere.ssl.SSLException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.Collections;

@ApplicationScoped
public class MongoProducer {
    // tag::mongoProducerInjections[]
    @Inject
    @ConfigProperty(name = "mongo.hostname", defaultValue = "localhost")
    String hostname;

    @Inject
    @ConfigProperty(name = "mongo.port", defaultValue = "27017")
    int port;

    @Inject
    @ConfigProperty(name = "mongo.dbname", defaultValue = "testdb")
    String dbName;

    // tag::username[]
    @Inject
    @ConfigProperty(name = "mongo.user")
    String user;
    // end::username[]

    // tag::encodedPassword[]
    @Inject
    @ConfigProperty(name = "mongo.pass.encoded")
    String encodedPass;
    // end::encodedPassword[]
    // end::mongoProducerInjections[]

    // tag::createMongo[]
    @Produces
    public MongoClient createMongo() throws SSLException {
        // tag::passwordUtil[]
        String password = PasswordUtil.passwordDecode(encodedPass);
        // end::passwordUtil[]
        MongoCredential creds = MongoCredential.createCredential(
                user,
                dbName,
                password.toCharArray()
        );

        // tag::sslContext[]
        SSLContext sslContext = JSSEHelper.getInstance().getSSLContext(
                "outboundSSLContext",
                Collections.emptyMap(),
                null
        );
        // end::sslContext[]

        return new MongoClient(
                new ServerAddress(hostname, port),
                creds,
                new MongoClientOptions.Builder()
                        // tag::sslEnable[]
                        .sslEnabled(true)
                        .sslContext(sslContext)
                        // end::sslEnable[]
                        .build()
        );
    }
    // end::createMongo[]

    @Produces
    public MongoDatabase createDB(MongoClient client) {
        return client.getDatabase(dbName);
    }

    public void close(@Disposes MongoClient toClose) {
        toClose.close();
    }
}