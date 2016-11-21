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

package fr.lig.sigma.astral.gui.graph.sugiyama;

import edu.uci.ics.jung.algorithms.layout.Layout;
import org.apache.commons.collections15.Transformer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

import static java.lang.Math.ceil;

/**
 * @author Loic Petit
 */
public class Node<E> {
    private E e;
    private static final JLabel lbl = new JLabel();
    private Layout<E, ?> layout;
    boolean virtual = false;

    public class Point {
        public double x;
        public double y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private Point nodeSize;
    Point p = new Point(0, 0);

    public Node(E e, Layout<E, ?> layout, Transformer<E, String> labelTransformer) {
        this.e = e;
        this.layout = layout;
        lbl.setText(labelTransformer.transform(e));

        Dimension size = lbl.getPreferredSize();
        //if(size.getWidth() == 0)
        //    nodeSize = new Point(100, 25);
        nodeSize = new Point(size.getWidth(),size.getHeight());

    }

    E getVertex() {
        return e;
    }

    Point getCtrPos() {
        return new Point(p.x+nodeSize.x/2,p.y+nodeSize.y/2);
    }

    Point getPos() {
        return p;
    }

    void setPos(double x, double y) {
        p = new Point(x, y);
        layout.setLocation(e, new Point2D.Double(x+nodeSize.x/2,y+nodeSize.y/2));
    }

    private int index = 0;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public Point getSize() {
        return nodeSize;
    }

    @Override
    public String toString() {
        return e+":{"+p.x+","+p.y+"}";
    }
}