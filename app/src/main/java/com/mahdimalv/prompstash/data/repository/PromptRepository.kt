package com.mahdimalv.prompstash.data.repository

import com.mahdimalv.prompstash.data.model.Prompt

interface PromptRepository {
    suspend fun getPrompts(): List<Prompt>
    suspend fun savePrompt(prompt: Prompt)
    suspend fun deletePrompt(id: String)
}

class FakePromptRepository @javax.inject.Inject constructor() : PromptRepository {
    private val prompts = mutableListOf(
        Prompt(
            id = "1",
            title = "Creative Storytelling Engine",
            body = "Act as a world-class literary editor with 20 years of experience. Provide feedback focusing on narrative hook, character agency, and thematic resonance. Format your response as an editorial letter, maintaining an encouraging yet rigorous tone.",
            tags = listOf("GPT4", "CLD"),
            isPublic = true,
        ),
        Prompt(
            id = "2",
            title = "Python Code Reviewer",
            body = "Review the following Python code for correctness, efficiency, and adherence to PEP 8 standards. Identify potential bugs, suggest improvements, and explain your reasoning.",
            tags = listOf("Code"),
        ),
        Prompt(
            id = "3",
            title = "Product Strategy Framework",
            body = "Act as a senior product strategist. Analyze the given product concept and provide a structured go-to-market strategy including target segments, positioning, key metrics, and a 90-day launch plan.",
            tags = listOf("Strategy"),
        ),
        Prompt(
            id = "4",
            title = "Email Polite Refusal",
            body = "Write a professional, empathetic email declining the following request. Maintain a warm tone while being clear about the refusal, and if possible suggest an alternative.",
            tags = listOf("Writing"),
        ),
        Prompt(
            id = "5",
            title = "Language Learning Partner",
            body = "Act as a patient language tutor. I am learning [LANGUAGE] at [LEVEL]. Engage me in natural conversation, gently correct my mistakes, explain grammar rules when relevant, and introduce new vocabulary in context.",
            tags = listOf("Education"),
        ),
    )

    override suspend fun getPrompts(): List<Prompt> = prompts.toList()

    override suspend fun savePrompt(prompt: Prompt) {
        val index = prompts.indexOfFirst { it.id == prompt.id }
        if (index >= 0) prompts[index] = prompt else prompts.add(prompt)
    }

    override suspend fun deletePrompt(id: String) {
        prompts.removeAll { it.id == id }
    }
}
