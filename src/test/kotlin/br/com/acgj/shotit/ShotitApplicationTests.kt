package br.com.acgj.shotit

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(InfraContainersForTestConfiguration::class)
@SpringBootTest
class ShotitApplicationTests : LocalstackTestContainerConfiguration(){

	@Test
	fun contextLoads() {
	}

}
