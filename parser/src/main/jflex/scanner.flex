/*
 * This file is part of AStrAL.
 *
 * AStrAL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with AStrAL.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009-2010 LIG SIGMA
 */
 
package fr.lig.sigma.astral.interpreter.lexer;

import fr.lig.sigma.astral.interpreter.parser.Constants;
import fr.lig.sigma.astral.interpreter.parser.sym;
import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;

%%

%class Lexer
%cup
%implements sym,Constants
%line   
%public
%column

%{
  private static final Logger log = Logger.getLogger(Lexer.class);

  private Symbol symbol(int sym) {
    return new Symbol(sym, yyline+1, yycolumn+1);
  }
  
  private Symbol symbol(int sym, Object val) {
    return new Symbol(sym, yyline+1, yycolumn+1, val);
  }
  
  private void error(String message) {
    log.error("Error at line "+(yyline+1)+", column "+(yycolumn+1)+" : "+message);
  }
%} 

Ident = [a-zA-Z] (("_" | [a-zA-Z0-9])* [a-zA-Z0-9] | [a-zA-Z0-9]*) | "\\infty"

IntLiteral = 0 | [-+]?[1-9][0-9]*

new_line = \r|\n|\r\n;

white_space = {new_line} | [ \t\f]

boperators = "<=" | "=" | "!=" | "<" | ">=" | ">"  

string = "\"" .* "\""
eval = "e_{" [^{}]* "}"
domain = "D_{" [^{}]* "}"
sigma = "\\sigma_{" [^{}]* "}"
join = "\\Join_{" [^{}]* "}"
                                     
%%

"\\Pi"       { return symbol(PI); }       
"\\rho"       { return symbol(RHO); }
"\\cup"       { return symbol(UNION); }
"\\times"       { return symbol(JOIN); }
"\\Join"       { return symbol(JOIN); }
"\\rhd"   { return symbol(SPREAD, null); }
"\\lhd"   { return symbol(SPREAD, "all"); }
"I_S"       { return symbol(STREAMERS, "Is"); }
"D_S"       { return symbol(STREAMERS, "Ds"); }
"R_S^condition"       { return symbol(STREAMERS, "Rsu"); }
"declare"   { return symbol(DECLARE); }
"SimpleRelation"   { return symbol(SIMPLE_RELATION); }   
"FileStream"   { return symbol(FILE_STREAM); }        
"FileRelation"   { return symbol(FILE_RELATION); } 
"SimpleStream"   { return symbol(SIMPLE_STREAM); }
"RemoteStream"   { return symbol(REMOTE_STREAM); }
"RemoteRelation"   { return symbol(REMOTE_RELATION); }   
"AccumulativeRelation"   { return symbol(ACCUMULATIVE_RELATION); }


/* names */
{sigma}       { return symbol(SIGMA, yytext().substring(8,yytext().length()-1)); }
{join}       { return symbol(JOIN, yytext().substring(7,yytext().length()-1)); }
{domain}       { return symbol(D, yytext().substring(3,yytext().length()-1)); }
{eval}      { return symbol(EVAL, yytext().substring(3,yytext().length()-1)); }
{Ident}           { return symbol(IDENT, yytext()); }   
{string}          { return symbol(STRING, yytext().substring(1,yytext().length()-1)); }
  
/* literals */
{IntLiteral} { return symbol(INTCONST, yytext()); }

{boperators} { return symbol(BOPERATORS, yytext()); }

                                        
"T0="               { return symbol(T0); }
"("               { return symbol(LPAR); }
")"               { return symbol(RPAR); }  
"{"               { return symbol(BEGIN); }
"}"               { return symbol(END); }
"["               { return symbol(LWIN); }
"]"               { return symbol(RWIN); }   
"_"               { return symbol(UNDERSCORE); }  
"/"               { return symbol(SLASH); } 
","               { return symbol(COMMA); }  
":"               { return symbol(COLON); }
";"               { return symbol(SEMICOLON); }      
"^"               { return symbol(ACCENT); }
"_G_"               { return symbol(G); } 

{white_space}     { /* ignore */ }
.|\n              {  /* throw new Error("Illegal character <"+ yytext()+">");*/
		    error("Illegal character <"+ yytext()+">");
                  }
