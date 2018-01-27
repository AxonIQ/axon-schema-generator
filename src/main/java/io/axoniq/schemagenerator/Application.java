package io.axoniq.schemagenerator;

import org.axonframework.eventhandling.saga.repository.jpa.AssociationValueEntry;
import org.axonframework.eventhandling.saga.repository.jpa.SagaEntry;
import org.axonframework.eventhandling.tokenstore.jpa.TokenEntry;
import org.axonframework.eventsourcing.eventstore.jpa.DomainEventEntry;
import org.axonframework.eventsourcing.eventstore.jpa.SnapshotEventEntry;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.InnoDBStorageEngine;
import org.hibernate.dialect.MySQL57Dialect;
import org.hibernate.dialect.Oracle12cDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class Application {

    enum RDBMS {
        MY_SQL_57_INNODB,
        ORACLE_12,
    }

    /* Set as appropriate. */
    static final RDBMS rdbms = RDBMS.ORACLE_12;

    public static void main(String[] args) {

        Map<String, Object> settings = new HashMap<>();

        switch(rdbms) {
            case MY_SQL_57_INNODB:
                settings.put("hibernate.dialect", MySQL57Dialect.class);
                settings.put("hibernate.dialect.storage_engine", InnoDBStorageEngine.class);
                /* See  https://vladmihalcea.com/why-should-not-use-the-auto-jpa-generationtype-with-mysql-and-hibernate/
                        https://hibernate.atlassian.net/browse/HHH-11014 */
                settings.put("hibernate.id.new_generator_mappings", false);
                break;
            case ORACLE_12:
                settings.put("hibernate.dialect", Oracle12cDialect.class);
                break;
        }

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();
        MetadataSources metadataSources = new MetadataSources(standardServiceRegistry);

        /* For event and snapshot storage - not needed if you fully run on AxonDB. */
        metadataSources.addAnnotatedClass(DomainEventEntry.class);
        metadataSources.addAnnotatedClass(SnapshotEventEntry.class);

        /* Needed for tracking event processors. */
        metadataSources.addAnnotatedClass(TokenEntry.class);

        /* Needed for sagas. */
        metadataSources.addAnnotatedClass(SagaEntry.class);
        metadataSources.addAnnotatedClass(AssociationValueEntry.class);

        switch(rdbms) {
            case ORACLE_12:
                /* Look into this file for explanation. */
                metadataSources.addResource("META-INF/orm.xml");
                break;
        }

        Metadata metadata = metadataSources.buildMetadata();

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        File outputFile = new File("V1__Initial_Axon_Setup.sql");
        if(outputFile.exists()) outputFile.delete();
        schemaExport.setOutputFile(outputFile.getAbsolutePath());
        schemaExport.createOnly(EnumSet.of(TargetType.STDOUT, TargetType.SCRIPT), metadata);
    }
}
