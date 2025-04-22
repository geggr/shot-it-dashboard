package br.com.acgj.shotit.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals

data class TestData(val name: String, val age: Int)

class JsonUtilsTest {
    @Test
    fun `should parse json string to object`() {
        val json = """{"name": "John", "age": 30}"""
        val result = parse(json, TestData::class.java)
        
        assertEquals("John", result.name)
        assertEquals(30, result.age)
    }
    
    @Test
    fun `should parse json string with nested objects`() {
        data class NestedData(val user: TestData)
        val json = """{"user": {"name": "Jane", "age": 25}}"""
        val result = parse(json, NestedData::class.java)
        
        assertEquals("Jane", result.user.name)
        assertEquals(25, result.user.age)
    }
} 