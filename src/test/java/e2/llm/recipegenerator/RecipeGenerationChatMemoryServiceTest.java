package e2.llm.recipegenerator;

import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RecipeGenerationChatMemoryServiceTest {
    @Autowired
    private RecipeSqlValidator sqlValidator;

    @Autowired
    private RecipeGenerationChatMemoryService sqlGeneratorChatService;

    @Test
    void testGenerateAndValidateAlterTableSQL() {
        String schemaJson = """
                {
                  "tableName": "sales",
                  "columns": [
                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
                    {"name": "tax_rate", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]}
                  ]
                }
                """;
        String userRequest = "Add a new computed column called total_with_tax of type DECIMAL(20,2) that multiplies product_price by quantity and then adds tax";
        String expectedSQL = "ALTER TABLE sales ADD COLUMN total_with_tax DECIMAL(20,2) GENERATED ALWAYS AS ((product_price * quantity) + (product_price * quantity * tax_rate))";

        sqlGeneratorChatService.initializeConversation(schemaJson, RecipeGenerationChatMemoryService.INSTRUCTIONS, "sales_preview.csv");

        String response = sqlGeneratorChatService.testConversation(userRequest);

        System.out.println("LLM response: " + response);
        assertTrue(sqlValidator.isValidSql(response));
        //assertEquals(expectedSQL, response.trim());
    }

}
