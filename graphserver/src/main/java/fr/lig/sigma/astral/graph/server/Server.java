/*
 * Copyright 2012 LIG SIGMA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.lig.sigma.astral.graph.server;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.handler.TupleSender;
import fr.lig.sigma.astral.query.GraphBuilder;
import fr.lig.sigma.astral.query.GraphNotifier;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 */
@Component
@Instantiate
@Provides
public class Server extends Thread implements GraphNotifier, TupleSender {
    private ServerSocket server;
    private static final Logger log = Logger.getLogger(Server.class);
    @Requires
    private AstralEngine engine;
    private GraphBuilder builder;
    private Collection<ServerConnection> connections = new LinkedList<ServerConnection>();

    @Validate
    public void validate() throws IOException {
        server = new ServerSocket(3000);
        log.info("Server created on port 3000");
        builder = new GraphBuilder(engine, this);
        this.start();
    }


    public void run() {
        while (true) {
            try {
                log.info("Waiting for connections.");
                Socket client = server.accept();
                log.info("Accepted a connection from: " + client.getInetAddress());
                connections.add(new ServerConnection(client, builder.getGraph()));
            } catch (IOException e) {
                log.error("Closing connection...", e);
                break;
            }
        }
        close();
    }

    
    public void sendGraph(Map<String, QueryNode> queries) {
        clean();
        for(ServerConnection c : connections)
            c.sendGraph(queries);
    }

    private void clean() {
        Iterator<ServerConnection> iterator = connections.iterator();
        while (iterator.hasNext()) {
            ServerConnection c = iterator.next();
            if(!c.isAlive()) {
                log.info("Removing a client as the connection is now closed");
                iterator.remove();
            }
        }
    }

    public void sendNotice(QueryNode node) {
        clean();
        for(ServerConnection c : connections)
            c.sendNotice(node);
    }

    public void sendTuple(String uuid, String desc, Tuple t) {
        clean();
        for(ServerConnection c : connections)
            c.sendTuple(uuid, desc, t);
    }

    public void close() {
        for(ServerConnection c : connections) {
            close();
        }
        connections.clear();
    }

}
