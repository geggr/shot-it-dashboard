package br.com.acgj.shotit

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
open class LocalstackTestContainerConfiguration {

    companion object {
        @Container
        val localstack = LocalStackContainer(DockerImageName.parse("localstack/localstack:2.1"))
            .withServices(LocalStackContainer.Service.S3)

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            // LocalStack properties
            registry.add("aws.endpoint") { localstack.getEndpoint() }
            registry.add("aws.region") { localstack.region }
            registry.add("aws.credentials.access-key") { localstack.accessKey }
            registry.add("aws.credentials.secret-key") { localstack.secretKey }
            localstack.start()
        }

        @JvmStatic
        fun verifyEmail() {
            val command = "awslocal ses verify-email-identity --email no-reply@shotit.com"
            val result = localstack.execInContainer(*command.split(" ").toTypedArray())
            if (result.exitCode != 0) {
                throw Exception(result.stderr)
            }
        }
    }
}