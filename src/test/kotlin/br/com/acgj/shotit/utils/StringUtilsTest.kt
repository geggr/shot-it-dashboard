package br.com.acgj.shotit.utils

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringUtilsTest {
    @Test
    fun `normalize filename with single space`() {
        val result = normalizeFilename("test file.txt")
        assertEquals("test_file.txt", result)
    }

    @Test
    fun `normalize filename with multiple spaces`() {
        val result = normalizeFilename("test  file   name.txt")
        assertEquals("test_file_name.txt", result)
    }

    @Test
    fun `normalize filename with tabs`() {
        val result = normalizeFilename("test\tfile.txt")
        assertEquals("test_file.txt", result)
    }

    @Test
    fun `normalize filename with newlines`() {
        val result = normalizeFilename("test\nfile.txt")
        assertEquals("test_file.txt", result)
    }

    @Test
    fun `normalize filename with no spaces`() {
        val result = normalizeFilename("testfile.txt")
        assertEquals("testfile.txt", result)
    }
} 