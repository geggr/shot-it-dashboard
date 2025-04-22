package br.com.acgj.shotit.core.infra.auth

import br.com.acgj.shotit.core.domain.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class JWTService(@Value("\${jwt.secret}") secret: String) {

    private final val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun generateToken(user: User): String = JWT.create().withIssuer("ClipIt").withClaim("email", user.email).withIssuedAt(
        Instant.now()).sign(algorithm)

    fun extractUserFromToken(token: String): String? {
        try {
            val jwt = JWT.require(algorithm).build().verify(token)
            return jwt.getClaim("email").asString()
        }
        catch (exception: JWTVerificationException){
            return null
        }
    }


}