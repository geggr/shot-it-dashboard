package br.com.acgj.shotit.core.infra.auth

import br.com.acgj.shotit.InfraContainersForTestConfiguration
import br.com.acgj.shotit.LocalstackTestContainerConfiguration
import br.com.acgj.shotit.core.domain.User
import br.com.acgj.shotit.core.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.UsernameNotFoundException

@DataJpaTest
@Import(UserDetailsServiceImpl::class, InfraContainersForTestConfiguration::class)
class UserDetailsServiceImplTest : LocalstackTestContainerConfiguration(){

    @Autowired
    private lateinit var userDetailsService: UserDetailsServiceImpl

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun `should return user details when user exists`() {
        val user = User(
            username = "johndoe",
            name = "John Doe",
            email = "john@example.com",
            password = "password123"
        )
        userRepository.save(user)

        val userDetails = userDetailsService.loadUserByUsername(user.email)

        assertThat(userDetails.username).isEqualTo(user.email)
        assertThat(userDetails.password).isEqualTo(user.password)
    }

    @Test
    fun `should throw username not found exception when user does not exist`() {
        assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername("nonexistent@example.com")
        }
    }
} 