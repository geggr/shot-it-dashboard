package br.com.acgj.shotit.core.domain

class RequestFieldError(val code: String, val message: String)
class RequestErrorResponse(val message: String, val errors: List<RequestFieldError>? = emptyList())

abstract class RequestError : RuntimeException(null, null, false, false) {
    abstract fun statusCode(): Int
    abstract fun error(): RequestErrorResponse
}

class BadRequestError : RequestError {
    private val error: RequestErrorResponse

    constructor(message: String): super() {
        this.error = RequestErrorResponse(message)
    }

    constructor(message: String, code: String, details: String): super(){
        this.error = RequestErrorResponse(
            message,
            listOf(RequestFieldError(code, details))
        )
    }

    constructor(message: String, errors: List<RequestFieldError>): super(){
        this.error = RequestErrorResponse(message, errors)
    }

    override fun statusCode(): Int = 400
    override fun error(): RequestErrorResponse = error
}

class UnauthorizedError(private val error: RequestErrorResponse = RequestErrorResponse("Unauthorized", listOf())) : RequestError() {
    override fun statusCode() = 403
    override fun error(): RequestErrorResponse = error
}

class NotFoundError : RequestError {
    private val error: RequestErrorResponse

    constructor(): super(){
        this.error = RequestErrorResponse("Not Found", listOf())
    }

    constructor(message: String): super(){
        this.error = RequestErrorResponse(message)
    }

    override fun statusCode(): Int = 404
    override fun error(): RequestErrorResponse = error
}