package br.com.acgj.shotit.core.infra.mailer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class EmailGeneratorTest {

    @Test
    fun `should generate email with title and body`() {
        val result = email {
            title("Welcome to ShotIt")
            body {
                p("Hello there!")
                p("Welcome to our platform")
            }
        }

        assertTrue(result.contains("<title>Welcome to ShotIt</title>"))
        assertTrue(result.contains("<p>Hello there!</p>"))
        assertTrue(result.contains("<p>Welcome to our platform</p>"))
    }

    @Test
    fun `should generate email with call to action`() {
        val result = email {
            title("Verify your email")
            body {
                p("Please verify your email address")
            }
            callToAction {
                message("Click the button below to verify")
                link("https://shotit.com/verify", "Verify Email")
            }
        }

        assertTrue(result.contains("Verify your email"))
        assertTrue(result.contains("Please verify your email address"))
        assertTrue(result.contains("Click the button below to verify"))
        assertTrue(result.contains("href=\"https://shotit.com/verify\""))
        assertTrue(result.contains("Verify Email"))
    }

    @Test
    fun `should generate email with strong text in body`() {
        val result = email {
            title("Important Update")
            body {
                p("This is an ")
                strong("important")
                p(" message")
            }
        }

        assertTrue(result.contains("<strong>important</strong>"))
    }

    @Test
    fun `should generate email with multiple call to actions`() {
        val result = email {
            title("Multiple Actions")
            body {
                p("Choose an action")
            }
            callToAction {
                message("First action")
                link("https://shotit.com/action1", "Action 1")
            }
            callToAction {
                message("Second action")
                link("https://shotit.com/action2", "Action 2")
            }
        }

        assertTrue(result.contains("First action"))
        assertTrue(result.contains("Second action"))
        assertTrue(result.contains("href=\"https://shotit.com/action1\""))
        assertTrue(result.contains("href=\"https://shotit.com/action2\""))
    }

    @Test
    fun `should generate email with empty title`() {
        val result = email {
            body {
                p("Content without title")
            }
        }

        assertTrue(result.contains("<title></title>"))
    }
} 