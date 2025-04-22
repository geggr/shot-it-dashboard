package br.com.acgj.shotit

import br.com.acgj.shotit.core.domain.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.assertj.core.api.Assertions.assertThat
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.core.MethodParameter
import java.lang.reflect.Method

class ShotitApplicationErrorHandlerTest {

    private val handler = ShotitApplicationErrorHandler()

    @Test
    fun `should handle method argument not valid exception with field errors`() {
        val method = this::class.java.getDeclaredMethod("testMethod", String::class.java)
        val parameter = MethodParameter(method, 0)
        val fieldError = FieldError("object", "field", "error message")
        val exception = MethodArgumentNotValidException(parameter, BindException("object", "objectName").apply {
            addError(fieldError)
        })

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Validation Error")
        assertThat(response.body?.errors).hasSize(1)
        assertThat(response.body?.errors?.first()?.code).isEqualTo("field")
        assertThat(response.body?.errors?.first()?.message).isEqualTo("error message")
    }

    @Test
    fun `should handle method argument not valid exception with global errors`() {
        val method = this::class.java.getDeclaredMethod("testMethod", String::class.java)
        val parameter = MethodParameter(method, 0)
        val globalError = ObjectError("object", "global error message")
        val exception = MethodArgumentNotValidException(parameter, BindException("object", "objectName").apply {
            addError(globalError)
        })

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Validation Error")
        assertThat(response.body?.errors).hasSize(1)
        assertThat(response.body?.errors?.first()?.code).isEqualTo("")
        assertThat(response.body?.errors?.first()?.message).isEqualTo("global error message")
    }

    @Test
    fun `should handle request error`() {
        val requestError = NotFoundError()

        val response = handler.handleRequestError(requestError)

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body?.message).isEqualTo("Not Found")
        assertThat(response.body?.errors).isEmpty()
    }

    @Test
    fun `should handle method argument not valid exception with field errors without default message`() {
        val method = this::class.java.getDeclaredMethod("testMethod", String::class.java)
        val parameter = MethodParameter(method, 0)
        val fieldError = FieldError("object", "field", "")
        val exception = MethodArgumentNotValidException(parameter, BindException("object", "objectName").apply {
            addError(fieldError)
        })

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Validation Error")
        assertThat(response.body?.errors).hasSize(1)
        assertThat(response.body?.errors?.first()?.code).isEqualTo("field")
        assertThat(response.body?.errors?.first()?.message).isEqualTo("")
    }

    @Test
    fun `should handle method argument not valid exception with global errors without code`() {
        val method = this::class.java.getDeclaredMethod("testMethod", String::class.java)
        val parameter = MethodParameter(method, 0)
        val globalError = ObjectError("object", "global error message")
        val exception = MethodArgumentNotValidException(parameter, BindException("object", "objectName").apply {
            addError(globalError)
        })

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Validation Error")
        assertThat(response.body?.errors).hasSize(1)
        assertThat(response.body?.errors?.first()?.code).isEqualTo("")
        assertThat(response.body?.errors?.first()?.message).isEqualTo("global error message")
    }

    @Test
    fun `should handle method argument not valid exception with global errors without default message`() {
        val method = this::class.java.getDeclaredMethod("testMethod", String::class.java)
        val parameter = MethodParameter(method, 0)
        val globalError = ObjectError("object", null)
        val exception = MethodArgumentNotValidException(parameter, BindException("object", "objectName").apply {
            addError(globalError)
        })

        val response = handler.handleMethodArgumentNotValidException(exception)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body?.message).isEqualTo("Validation Error")
        assertThat(response.body?.errors).hasSize(1)
        assertThat(response.body?.errors?.first()?.code).isEqualTo("")
        assertThat(response.body?.errors?.first()?.message).isEqualTo("")
    }

    private fun testMethod(param: String) {}
} 