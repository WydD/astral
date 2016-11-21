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

package fr.lig.sigma.astral.common.structure.containers;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.*;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Abstract entity container that helps the user to create a container. It is used by {@link RelationContainer} and
 * {@link StreamContainer}. This class should never be directly implemented by the user.
 *
 * @author Loic Petit
 * @see fr.lig.sigma.astral.common.structure.EntityContainer
 */
@Component
public abstract class AbstractEntityContainer<E extends Entity> extends EventNotifier implements EntityContainer<E>, VolatileStructure {
    protected EntityFactory entityFactory;

    @Bind(id = "entityFactory", specification = "fr.lig.sigma.astral.common.structure.EntityFactory")
    public synchronized void bindEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Unbind(id = "entityFactory", specification = "fr.lig.sigma.astral.common.structure.EntityFactory")
    public synchronized void unbindEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = null;
    }

    /**
     * Proxy entity that is used to represents this class
     */
    protected E output;

    public String getName() {
        if (output == null) return "N/A";
        return output.getName();
    }

    @Override
    public String toString() {
        if (output == null) return "N/A";
        return output.toString();
    }

    @Override
    public void setScheduler(EventScheduler scheduler) {
        if (output != null)
            output.setScheduler(scheduler);
        super.setScheduler(scheduler);
    }

    @Override
    public EventScheduler getScheduler() {
        return super.getScheduler();
    }

    public Set<String> getAttributes() {
        if (output == null) return null;
        return output.getAttributes();
    }

    public void registerNotifier(EventProcessor e) {
        output.registerNotifier(e);
    }

    @Override
    public void forgetDataBefore(Batch batch) {
        if (output != null && output instanceof VolatileStructure)
            ((VolatileStructure) output).forgetDataBefore(batch);
    }

    /**
     * Create an entity with the same structure as the given entity. The algorithm tries to trace the original entity
     * parents to find its core implementation. If no known implementation is found the algorithm put by default basic
     * implementations.
     *
     * @param e          the entity to copy the structure
     * @param attributes the entity's future attributes
     * @param name       the entity's future name
     * @return the created entity
     * @throws RuntimeException When any exception has been threw when creating the entity (event a default one)
     */
    @SuppressWarnings({"all", "unchecked"})
    public E createNewFrom(E e, Set<String> attributes, String name) {
        return createNewFrom(e, attributes, name, null);
    }

    public E createNewFrom(E e, Set<String> attributes, String name, Map<String, String> properties) {
        Class c = e.getClass();
        /* OMYGOD THIS THING IS SO SILLY, BUT I'VE NO OTHER IDEA IN MIND */
        /*while (!c.getPackage().getName().startsWith("fr.lig.sigma.astral.common.structure.impl")) {
            c = c.getSuperclass();
            if (c == null) break;
        } */
        String type;
        /*if (c == null) {
          */
        if (e instanceof Stream) type = "StreamQueueImpl";
        else if (e instanceof DynamicRelation && this instanceof DynamicRelation) type = "NativeDynamicRelationImpl";
        else type = "RelationBufferedVolatileImpl";
        /*} else
            type = c.getName();
        /*try {
            if (c != null)
                return (E) c.getConstructor(Set.class, String.class).newInstance(attributes, name);
        } catch (Exception ex) {
            throw new RuntimeException("A weird thing happened... I was unable to create a duplicate of " + e + "\n" + ex);
        } */
        try {
            if (properties == null)
                return (E) entityFactory.instanciateEntity(type, name, attributes);
            else
                return (E) entityFactory.instanciateEntity(type, name, attributes, new Hashtable<String, Object>(properties));
        } catch (Exception ex) {
            throw new RuntimeException("A weird thing happened... I was unable to create a duplicate of " + e + "\n" + ex);
        }
    }

    public Entity getOutput() {
        return output;
    }

    /**
     * Set the output proxy and binds the scheduler
     *
     * @param output The proxy entity
     */
    public void setOutput(E output) {
        if (this.output != null && entityFactory != null)
            entityFactory.disposeEntity(this.output);
        this.output = output;
        setScheduler(super.getScheduler());
    }

    @SuppressWarnings({"unchecked"})
    public boolean setOutputAttributes(Set<String> attributes) {
        String type = output.getClass().getName();
        String name = output.getName();
        entityFactory.disposeEntity(output);
        setOutput((E) entityFactory.instanciateEntity(type, name, attributes));
        return true;
    }
}
