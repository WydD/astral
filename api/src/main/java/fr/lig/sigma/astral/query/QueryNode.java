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

package fr.lig.sigma.astral.query;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
public class QueryNode implements Serializable {
    private String name;
    private Map<String, Object> parameters;
    private List<QueryNode> children;
    private long id;
    private String indent = "";

    public QueryNode(String name, Map<String, Object> parameters, List<QueryNode> children, String id) {
        this.name = name;
        this.parameters = parameters;
        this.children = children;
        this.id = Long.parseLong(id);
        if(children != null)
            incIndent();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryNode queryNode = (QueryNode) o;

        return id == queryNode.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public List<QueryNode> getChildren() {
        return children;
    }

    private void incIndent() {
        indent += "        ";
        for(QueryNode node : children) {
            node.incIndent();
        }
    }

    @Override
    public String toString() {
        return "\n"+indent + name +" {\n" +
                indent + "    parameters=" + parameters + "\n" +
                indent + "    children= " + children + "\n" +
                indent +'}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
