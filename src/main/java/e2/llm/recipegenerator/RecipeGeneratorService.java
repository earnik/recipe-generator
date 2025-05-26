package e2.llm.recipegenerator;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeGeneratorService {

    private static final String[] INSTRUCTIONS = {
        "You are an expert SQL query generator. Your task is to generate SQL queries based on natural language requests and the database schema provided.",
        "Use ONLY the table and column names from the provided schema above",
        "Generate a valid SQL query based on the user's request",
        "Only return the SQL query with no additional text or explanations",
        "Ensure the query is properly formatted and syntactically correct",
        "Pay close attention to the action the SQL command should perform (whether it is a SELECT, ALTER TABLE, etc.)",
        "Make sure the query is compatible with most SQL database systems",
        "do not generate ```sql wrapping around the query, just return the query itself",
        "Remove wrapping ```sql around the result query, just return the query itself",
        "If you need to make assumptions, choose the most likely implementation"
    };

    private final ChatClient chatClient;
    private final RecipeSqlValidator sqlValidator;

    public RecipeGeneratorService(ChatClient chatClient, RecipeSqlValidator sqlValidator) {
        this.chatClient = chatClient;
        this.sqlValidator = sqlValidator;
    }

    public String generateSQLQuery(String tableSchema, String userRequest) {
        return generateSQLQuery(tableSchema, userRequest, false);
    }

    public String generateSQLQuery(String tableSchema, String userRequest, boolean validate) {
        // Create a system prompt template with schema information
        String systemPromptTemplate = """
            You are an expert SQL query generator. Your task is to generate SQL queries based on
            natural language requests and the database schema provided.

            THE DATABASE SCHEMA IS PROVIDED BELOW:
            {schema}

            INSTRUCTIONS:
            {instructions}
            """;

            SystemPromptTemplate systemPrompt = new SystemPromptTemplate(systemPromptTemplate);
            Map<String, Object> model = new HashMap<>();
            model.put("schema", tableSchema);
            model.put("instructions", INSTRUCTIONS);

            Message systemMessage = systemPrompt.createMessage(model);
            Message userMessage = new UserMessage(userRequest);

            List<Message> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMessage);

            String generatedSql = chatClient
                    .prompt()
                    .messages(messages)
                    .user(userRequest)
                    .call()
                    .content();

            // Validate SQL if requested and validator is available
            if (validate && sqlValidator != null) {
                if (!sqlValidator.isValidSql(generatedSql)) {
                    String error = sqlValidator.getValidationError(generatedSql);
                    throw new IllegalStateException("Generated SQL is invalid: " + error);
                }
            }

            return generatedSql;
        }
    }
