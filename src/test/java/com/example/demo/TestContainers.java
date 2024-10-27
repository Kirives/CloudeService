package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class TestContainers {

    @Container
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("sa")
            .withPassword("123");

    @Container
    private static MinIOContainer container = new MinIOContainer("minio/minio:RELEASE.2023-09-04T19-57-37Z")
            .withEnv("MINIO_ACCESS_KEY", "minioadmin")
            .withEnv("MINIO_SECRET_KEY", "minioadmin");

    @BeforeAll
    static void setup(){
        // Настройка PostgreSQL
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());

        // Настройка Minio
        String minioUrl = "http://" + container.getHost() + ":" + container.getMappedPort(9000);
        System.setProperty("minio.url", minioUrl);
        System.setProperty("minio.access-key", "minioadmin");
        System.setProperty("minio.secret-key", "minioadmin");
    }
}
