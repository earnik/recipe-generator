package e2.llm.recipegenerator;

import org.springframework.stereotype.Service;

@Service
public class RecipeGenerationChatMemoryService {
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
            "If you need to make assumptions, choose the most likely implementation",

            "If the user's request is ambiguous or could be interpreted in multiple ways," +
                    " ASK A CLARIFYING QUESTION instead of making assumptions. Be specific about the options." +
                    " Only generate SQL when you have all the information needed to create the correct query."
    };

    // TODO: continue here - implement the chat service logic to generate SQL queries based on user requests and schema information.
    // https://claude.ai/chat/d7a2c041-5182-48a4-9ce6-bc005d09dc1b



}
