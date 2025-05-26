package e2.llm.recipegenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

@Service
public class RecipeGenerationChatMemoryService {
    private static final String[] INSTRUCTIONS = {
            "You are an expert SQL query generator. Your task is to generate SQL queries based on natural language requests and the database schema provided.",
            "Use ONLY the table and column names from the provided schema above",
            "It is more important to return an accurate query than an assumed one. Therefore, if you feel any of the user fields are ambiguous, " +
            "ask a clarifying question instead of generating an SQL query.",
            "Generate a valid SQL query based on the user's request unless you want to ask a clarifying question",
            "Ensure the query is properly formatted and syntactically correct",
            "Pay close attention to the action the SQL command should perform (whether it is a SELECT, ALTER TABLE, etc.)",
            "Make sure the query is compatible with most SQL database systems",
            "do not generate ```sql wrapping around the query, just return the query itself",
            "Remove wrapping ```sql around the result query, just return the query itself",

            "Only return the SQL query with no additional text or explanations unless you want to ask a clarifying question",

            "If the user's request is ambiguous, incomplete, or could be interpreted in multiple ways, " +
            "ASK A CLARIFYING QUESTION instead of generating an SQL query. " +
            "Be specific about the options or missing information. " +
            "Only generate SQL when you have all the information needed to create the correct query.",

            "Think about each of the user provided fields and how they might be misinterpreted. " +
            "For example, ask if a given tax_rate is already divided by 100 or not.",

            "Don't make assumptions about the user's intent without asking for clarification. "
    };

    private final ChatClient chatClient;
    private final ChatMemory chatMemory; // Injected or provided ChatMemory
    private final ChatModel chatModel; // Optional: if you need to specify a model

    private Message systemMessage; // To store the initialized system message
    private String conversationId; // Unique identifier for the conversation

    public RecipeGenerationChatMemoryService(ChatClient chatClient, ChatMemory chatMemory, ChatModel chatModel) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.chatModel = chatModel;
    }

    /**
     * Initializes the conversation with the database schema and system instructions.
     * This should be called once before starting a new SQL generation conversation.
     * @param tableSchema The database schema description.
     */
    public void initializeConversation(String tableSchema) {
        String systemPromptTemplateText = """
            You are an expert SQL query generator. Your task is to generate SQL queries based on
            natural language requests and the database schema provided.
            
            THE DATABASE SCHEMA IS PROVIDED BELOW:
            {schema}

            INSTRUCTIONS:
            {instructions}
            """;

        int conversationIdInt = (new Random()).nextInt(1000); // Generate a random conversation ID for this session
        conversationId = String.valueOf(conversationIdInt);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptTemplateText);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", tableSchema);
        model.put("instructions", INSTRUCTIONS); // Use the combined instructions string

        this.systemMessage = systemPromptTemplate.createMessage(model);

        // clean up previous conversation memory
        this.chatMemory.clear(conversationId);

        // Add system message to chat memory
        this.chatMemory.add(conversationId, this.systemMessage);
    }

    /**
     * Generates an SQL query or asks a clarifying question based on the user request and conversation history.
     * @param userRequest The user's natural language request or answer to a clarification.
     * @return The generated SQL query or a clarification question from the LLM.
     */
    public String generateResponse(String userRequest) {
        if (this.systemMessage == null) {
            throw new IllegalStateException("Conversation not initialized. Call initializeConversation() first with the table schema.");
        }

        // Add current user message to chat memory
        UserMessage currentUserMessage = new UserMessage(userRequest);
        this.chatMemory.add(conversationId, currentUserMessage);

        ChatResponse chatResponse = chatModel.call(new Prompt(chatMemory.get(conversationId))); // Use chat memory for the conversation;
        AssistantMessage modelOutput = chatResponse.getResult().getOutput();
        chatMemory.add(conversationId, modelOutput); // Add the response to memory

        return modelOutput.getText();
    }

    public String testConversation(String userRequest) {
        String response = generateResponse(userRequest);
        System.out.println("Turn 0: " + response);
        int maxTurns = 5; // Prevent infinite loops
        int turn = 0;

        Scanner scanner = new Scanner(System.in);
        System.out.println("type 'quit' to exit or else press Enter");
        String userInput = scanner.nextLine();

        while ((response != null && isLikelyQuestion(response) || (!userInput.trim().equalsIgnoreCase("quit"))) && turn < maxTurns) {
            System.out.println("Turn " + (turn+1) + ": " + response);
            System.out.print("User clarification: ");
            userInput = scanner.nextLine();
            if (userInput.equals("quit")) {
                break; // Exit the loop if user types "quit"
            }
            response = generateResponse(userInput);
            turn++;
        }

        return response;
    }

    private static boolean isLikelyQuestion(String response) {
        String lowerResponse = response.toLowerCase().trim();
        return lowerResponse.contains("?") ||
                lowerResponse.startsWith("do you want") ||
                lowerResponse.startsWith("should i") ||
                lowerResponse.startsWith("would you like") ||
                lowerResponse.contains("clarify") ||
                lowerResponse.contains("which option") ||
                lowerResponse.contains("please specify") ||
                lowerResponse.contains("need to know") ||
                lowerResponse.contains("could you clarify");
    }
}
