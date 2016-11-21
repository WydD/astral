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

package fr.lig.sigma.astral.gui.source;

import fr.lig.sigma.astral.source.Source;
import fr.lig.sigma.astral.source.SourceFactory;

import javax.swing.*;
import java.util.Properties;

/**
 * @author Loic Petit
 */
@SuppressWarnings("unchecked")
public class SourceFactoryFromModel {
    public static Source createSource(SourceModel source,
                                      //NetworkFactory nf,
                                      SourceFactory sf) {
        Properties p = new Properties();
        p.put("entityname", source.getName());
        if (source instanceof SimpleEntityModel) {
            SimpleEntityModel s = (SimpleEntityModel) source;
            p.put("rate", s.getRate());
            p.put("card", s.getCard());
        } else if (source instanceof FileEntityModel) {
            FileEntityModel s = (FileEntityModel) source;
            p.put("file", s.getSourceFile());
        } else if (source instanceof RemoteEntityModel) {
            try {
                RemoteEntityModel s = (RemoteEntityModel) source;
                return null;//nf.createRemote(s.getAddress(), s.getPort(), s.getType().indexOf("Stream") >= 0);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e, "Network Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
        } else if (source instanceof ShivaEntityModel) {

        }
        try {
            return sf.createSource(source.getType(), p);
        } catch (Exception e) {
            throw new IllegalStateException("WRONG Source type ! " + source.getType() + "\n" + e.getMessage());
        }
    }
}
