#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
OpenL simple project.

PROJECT STRUCTURE

Project structure uses default Maven project structure and contains the following files:

        |-- ${artifactId}                        Project root folder
        |  |-- pom.xml                                  Maven project file
        |  |  
        |  |-- assembly                                 Maven assembly plugin configurations
        |  |
        |  |  |-- runnable-zip.xml                      Assembly configuration for runnable console application
        |  |  |-- deployable-zip.xml                    Assembly configuration for deployable to WebStudio zip
        |  
        |  |-- src
        |  |  
        |  |  |-- main
        |  |  |  
        |  |  |  |-- java
        |  |  |  |  
        |  |  |  |  |-- template
        |  |  |  |  |  |-- Main.java                    Sample class which uses wrapper class
        |  |  |  |  |  |-- Wrapper.java                 An interface class which holds rules method definitions
        |  |  |  
        |  |  |  |-- openl                              This folder contains all OpenL-related resources (rules, xml etc.)
        |  |  |  |  
        |  |  |  |  |-- rules.xml                       OpenL project descriptor (for OpenL only)
        |  |  |  |  |-- rules
        |  |  |  |  |  |-- TemplateRules.xls            File with rules
        |  |  |  
        |  |  |  |-- scripts
        |  |  |  |  
        |  |  |  |  |-- start.cmd                       Script that runs the Main class for Windows
        |  |  |  |  |-- start.sh                        Script that runs the Main class for Linux


WRAPPER CLASS

Current project uses OpenL dynamic wrapper to show how OpenL rules can be used in java application. 

For more information about OpenL Tablets visit our site http://openl-tablets.sourceforge.net .

OPENL PROJECT DESCRIPTOR

OpenL project descriptor is just simple xml file which contains information about current OpenL project. It is used 
by OpenL WebStudio and web services deploy manager to obtain information about project.

For more information about OpenL Tablets visit our site http://openl-tablets.sourceforge.net .

CONFIGURATION CHANGING

The "Main" java class demonstrates how can be used OpenL Tablets using file with rules directly. "rules.xml" also contains 
reference to it. If you need to make changes concerning rules file (e.g. rename, move) you have to maintain "rules.xml" manually.