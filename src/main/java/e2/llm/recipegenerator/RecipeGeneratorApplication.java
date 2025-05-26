package e2.llm.recipegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RecipeGeneratorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RecipeGeneratorApplication.class, args);

        RecipeGenerationChatMemoryService recipeGenerationChatMemoryService = context.getBean(RecipeGenerationChatMemoryService.class);

//        String schemaJson = """
//                {
//                  "tableName": "sales",
//                  "columns": [
//                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
//                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
//                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
//                    {"name": "tax_rate", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]}
//                  ]
//                }
//                """;
//        String userRequest = "Add a new computed column called total_with_tax of type DECIMAL(20,2) that multiplies product_price by quantity and then adds tax";
//        String expectedSQL = "ALTER TABLE sales ADD COLUMN total_with_tax DECIMAL(20,2) GENERATED ALWAYS AS ((product_price * quantity) + (product_price * quantity * tax_rate))";

                String schemaJson = """
                {
                  "tableName": "sales",
                  "columns": [
                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
                    {"name": "tax", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]},
                    {"name": "percentage", "type": "DECIMAL(3,2)", "constraints": ["NOT NULL"]}
                  ]
                }
                """;
        String userRequest = "Add a new computed column called total_with_tax of type DECIMAL(20,2) that multiplies product_price by percentage and then adds tax";

        recipeGenerationChatMemoryService.initializeConversation(schemaJson);

        String response = recipeGenerationChatMemoryService.testConversation(userRequest);
        System.out.println("LLM FINAL RESPONSE: " + response);
    }
}
