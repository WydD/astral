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

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.query.GraphNotifier;
import fr.lig.sigma.astral.query.QueryNode;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

/**
 *
 */
public class ServerConnection implements GraphNotifier {
    private Socket client = null;
    private ObjectOutputStream oos = null;


    public ServerConnection(Socket clientSocket, Map<String, QueryNode> graph) {
        client = clientSocket;
        try {
            oos = new ObjectOutputStream(client.getOutputStream());
            sendGraph(graph);
        } catch (Exception e1) {
            try {
                client.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    public synchronized void sendGraph(Map<String, QueryNode> queries) {
        try {
            oos.writeObject(Messages.GRAPH);
            oos.writeUnshared(queries);

            oos.flush();
        } catch (Exception e) {
        }
    }

    @Override
    public synchronized void sendNotice(QueryNode node) {
        try {
            oos.writeObject(Messages.NOTICE);
            oos.writeObject(node.getId());

            oos.flush();
        } catch (Exception e) {
        }
    }

    public void close() {
        try {
            // close streams and connections
            oos.close();
            client.close();
        } catch (Exception e) {
        }
    }

    public synchronized void sendTuple(String uuid, String desc, Tuple t) {
        try {
            oos.writeObject(Messages.TUPLE);
            oos.writeObject(uuid);
            oos.writeObject(desc);
            oos.writeObject(t);

            oos.flush();
        } catch (Exception e) {
        }
    }

    public boolean isAlive() {
        return client.isConnected();
    }
}
