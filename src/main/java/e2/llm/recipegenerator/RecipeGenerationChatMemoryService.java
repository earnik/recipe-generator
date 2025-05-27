package e2.llm.recipegenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

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
    public static final String[] INSTRUCTIONS = {
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

    public static final String[] SMART_INSTRUCTIONS = {
            "You are an expert SQL query generator. Your primary objective is to assist users by generating accurate SQL queries based on their natural language requests and the provided database schema.",

            "**Core Principle: Accuracy through Clarification**",
            "It is FAR MORE IMPORTANT to return an accurate query than an assumed one. Your default behavior when faced with any ambiguity or missing information MUST BE to ask a clarifying question. Do not generate SQL if you have doubts.",

            "**Schema Adherence:**",
            "1. Use ONLY the table and column names explicitly defined in the provided database schema.",
            "2. Do NOT invent column names or assume the existence of tables/columns not listed in the schema.",

            "**Ambiguity Detection and Clarification Protocol:**",
            "You MUST ask a clarifying question if:",

            "1. **Unclear User-Provided Fields/Concepts:**",
            "    *   The user mentions a field, concept, value, or calculation logic that is NOT explicitly present in the schema OR whose interpretation within the schema is ambiguous.",
            "    *   **Example (like your 'tax' case):** If a user mentions 'tax', 'discount', 'fee', 'rate', or any similar term involved in a calculation:",
            "        *   **Question its nature:** Is it a percentage (e.g., 5 for 5%), a decimal rate (e.g., 0.05), a flat amount, or something else?",
            "        *   **Question its application:** If it's a rate/percentage, does it apply to a single column (e.g., `product_price`) or a sub-calculation (e.g., `product_price * quantity`)?",
            "        *   **Question its pre-processing:** For rates/percentages, ask if it's already in a ready-to-use decimal form (e.g., 0.05) or if it needs division by 100 (e.g., if input is 5 for 5%).",
            "    *   **General approach:** For ANY field mentioned by the user that is not directly a column name with an obvious interpretation for the requested operation, probe its meaning and how it should be used.",

            "2. **Ambiguous Operations or Logic:**",
            "    *   The user's description of an operation (e.g., \"update records,\" \"find averages,\" \"group by\") lacks sufficient detail for an unambiguous query. For instance, \"update product price\" - by how much? For which products?",
            "    *   The desired filtering conditions are vague (e.g., \"recent orders,\" \"top customers\"). Ask for specific criteria (e.g., \"orders in the last X days,\" \"customers with the highest Y\").",

            "3. **Computed Columns/Values:**",
            "    *   When asked to create a computed column or derive a value, if the formula or any of its components are not explicitly defined or obvious from the schema, ASK. For example, if asked for `total_sales`, ask if it's `SUM(price)` or `SUM(price * quantity)`.",

            "**How to Ask Clarifying Questions:**",
            "*   When you need to ask a question, DO NOT generate ANY SQL query.",
            "*   State your question(s) clearly and concisely.",
            "*   Be specific about the ambiguity. If possible, offer potential options or interpretations the user can choose from.",
            "    *   *Good Example (for the tax scenario):* \"To calculate `total_with_tax`, I need a bit more information about 'tax'. Could you clarify:",
            "        1. Is 'tax' a fixed amount, a percentage (e.g., 5 for 5%), or a decimal rate (e.g., 0.05)?",
            "        2. If it's a percentage/rate, should I apply it to `product_price` alone, or to `product_price * quantity`?",
            "        3. If it's a percentage (like 5), should I use it as `0.05` in the calculation?\"",

            "**SQL Generation Guidelines (Only After Clarification):**",
            "*   Once all ambiguities are resolved, generate a valid SQL query.",
            "*   Pay close attention to the SQL command type (SELECT, ALTER TABLE, INSERT, UPDATE, DELETE, etc.).",
            "*   Ensure the query is syntactically correct and compatible with most common SQL database systems (e.g., ANSI SQL).",
            "*   Return ONLY the SQL query itself, with no surrounding text, explanations, or ```sql``` markdown, unless you are asking a clarifying question.",

            "**Final Check:**",
            "Before outputting SQL, ask yourself: \"Am I making any assumptions about user intent or data interpretation that I haven't confirmed?\" If yes, ask a question instead."
    };

    private final ChatMemory chatMemory; // Injected or provided ChatMemory
    private final ChatModel chatModel; // Optional: if you need to specify a model

    private Message systemMessage; // To store the initialized system message
    private String conversationId; // Unique identifier for the conversation

    public RecipeGenerationChatMemoryService(ChatMemory chatMemory, ChatModel chatModel) {
        this.chatMemory = chatMemory;
        this.chatModel = chatModel;
    }

    /**
     * Initializes the conversation with the database schema and system instructions.
     * This should be called once before starting a new SQL generation conversation.
     * @param tableSchema The database schema description.
     */
    public void initializeConversation(String tableSchema, String[] instructions, String previewCsvPath) {
        String systemPromptTemplateText = """
            You are an expert SQL query generator. Your task is to generate SQL queries based on
            natural language requests and the database schema provided.
            
            THE DATABASE SCHEMA IS PROVIDED BELOW:
            {schema}
            
            TABLE PREVIEW:
            {preview}
            
            INSTRUCTIONS:
            {instructions}
            """;

        int conversationIdInt = (new Random()).nextInt(1000); // Generate a random conversation ID for this session
        conversationId = String.valueOf(conversationIdInt);

        String previewContents = readPreviewCsv(previewCsvPath);
//        System.out.println("Preview contents: \n" + previewContents);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptTemplateText);
        Map<String, Object> model = new HashMap<>();
        model.put("schema", tableSchema);
        model.put("instructions", instructions); // Use the combined instructions string
        model.put("preview", previewContents);

        this.systemMessage = systemPromptTemplate.createMessage(model);

        // clean up previous conversation memory
        this.chatMemory.clear(conversationId);

        // Add system message to chat memory
        this.chatMemory.add(conversationId, this.systemMessage);
    }

    private static String readPreviewCsv(String previewCsvPath) {
        // Implement logic to read the CSV file and return its contents as a string
        // For simplicity, let's assume the CSV is small and can be read into memory
        // In a real application, we might want to handle larger files differently
        try (Scanner scanner = new Scanner(Objects.requireNonNull(RecipeGenerationChatMemoryService.class.getResourceAsStream("/" + previewCsvPath)))) {
            StringBuilder previewContents = new StringBuilder();
            while (scanner.hasNextLine()) {
                previewContents.append(scanner.nextLine()).append("\n");
            }
            return previewContents.toString().trim(); // Remove trailing newline
        } catch (Exception e) {
            throw new RuntimeException("Failed to read preview CSV: " + e.getMessage(), e);
        }
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
        System.out.println("User Request: " + userRequest);
        String response = generateResponse(userRequest);
        System.out.println("LLM Response: " + response);
        int maxTurns = 5; // Prevent infinite loops
        int turn = 0;

        Scanner scanner = new Scanner(System.in);

        while ((response != null && isLikelyQuestion(response)) && turn < maxTurns) {
            System.out.print("Iteration " + (turn+1) + " - User clarification or 'quit': ");
            String userInput = scanner.nextLine();
            if (userInput.equals("quit")) {
                break; // Exit the loop if user types "quit"
            }
            response = generateResponse(userInput);
            System.out.println("LLM Response: " + response);
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
