# axon-schema-generator

The goal of this project is to make it easy to generate DDL scripts for Axon Framework applications using JPA.
Hibernate can automatically create the required tables on startup, but that's not a very suitable approach
for production environments. This project can be used to let Hibernate export the DDL to a separate file,
which can then be used in e.g. a Flyway setup.

This project also contains some advice on database-specific setup, especially around the generation of the
global index of events.

This project started to tackle a particular presales question from an individual prospect and is currently
a program which is configured by modifying its source. We'll further develop this into a full command line
utility to create DDL scripts for Axon Framework JPA entities.
