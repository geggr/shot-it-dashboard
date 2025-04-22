package br.com.acgj.shotit

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration


@TestConfiguration(proxyBeanMethods = false)
open class InfraContainersForTestConfiguration {


    private lateinit var mysql: MySQLContainer<*>

    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> {
        mysql = MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
        return mysql
    }


    private lateinit var rabbitmq: RabbitMQContainer

    @Bean
    @ServiceConnection
    fun rabbitmqContainer(): RabbitMQContainer {
        rabbitmq = RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
            .withExchange("video.event", "topic")
            .withQueue("video.processed")
            .withQueue("video.thumbnail")
            .withBinding("video.event", "video.processed", mapOf(), "video.upload", "topic")
            .withBinding("video.event", "video.thumbnail", mapOf(), "thumbnail.generated", "topic")
        return rabbitmq
    }



} 