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

package fr.lig.sigma.astral.core.operators.relational.evaluate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class JSKeywords {
    private static Set<String> keywords = new HashSet<String>(Arrays.asList("abstract",
            "else",
            "instanceof",
            "super",
            "boolean",
            "enum",
            "int",
            "switch",
            "break",
            "export",
            "interface",
            "synchronized",
            "byte",
            "extends",
            "let",
            "this",
            "case",
            "false",
            "long",
            "throw",
            "catch",
            "final",
            "native",
            "throws",
            "char",
            "finally",
            "new",
            "transient",
            "class",
            "float",
            "null",
            "true",
            "const",
            "for",
            "package",
            "try",
            "continue",
            "function",
            "private",
            "typeof",
            "debugger",
            "goto",
            "protected",
            "var",
            "default",
            "if",
            "public",
            "void",
            "delete",
            "implements",
            "return",
            "volatile",
            "do",
            "import",
            "short",
            "while",
            "double",
            "in",
            "static",
            "with",
            "Math",
            "Date"));

    public static boolean isKeyword(String a) {
        return keywords.contains(a);
    }
}
