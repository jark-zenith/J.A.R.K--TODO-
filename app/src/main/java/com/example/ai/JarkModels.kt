package com.example.ai

import com.example.data.model.JarkActionName
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String? = null
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@Serializable
data class JarkAiActionPayload(
    val action: String = "none",
    val title: String? = null,
    val description: String? = null,
    val priority: String? = null,
    val category: String? = null,
    val dueDate: String? = null,
    val status: String? = null,
    val tags: List<String>? = null,
    val query: String? = null,
    val reply: String? = null
)
