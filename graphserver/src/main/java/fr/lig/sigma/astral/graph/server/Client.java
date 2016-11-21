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
import fr.lig.sigma.astral.handler.TupleSender;
import fr.lig.sigma.astral.query.GraphNotifier;
import fr.lig.sigma.astral.query.QueryNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Map;

/**
 *
 */
public class Client extends Thread {
    private String address;
    private GraphNotifier notifier;
    private TupleSender tupleNotif;

    public Client(String address, GraphNotifier notifier, TupleSender tupleNotif) {
        this.address = address;
        this.notifier = notifier;
        this.tupleNotif = tupleNotif;
        start();
    }

    public void run() {
        try {
            Socket s = new Socket(address, 3000);
            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            while(!s.isClosed()) {
                Messages m = (Messages) ois.readObject();
                switch (m) {
                    case GRAPH:
                        //System.out.println("Receiving Graph");
                        Map<String, QueryNode> graph = (Map<String, QueryNode>) ois.readObject();
                        notifier.sendGraph(graph);
                        break;
                    case NOTICE:
                        //System.out.println("Receiving Notice");
                        Long i = (Long) ois.readObject();
                        notifier.sendNotice(new QueryNode(null, null, null, i.toString()));
                        break;
                    case TUPLE:
                        //System.out.println("Receiving Tuple");
                        String uuid = (String) ois.readObject();
                        String description = (String) ois.readObject();
                        Tuple t = (Tuple) ois.readObject();
                        tupleNotif.sendTuple(uuid, description, t);
                        break;
                }
            }
            ois.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ClientGui gui = new ClientGui();
        new Client(args[0], gui.getGraph(), gui);
    }
}
