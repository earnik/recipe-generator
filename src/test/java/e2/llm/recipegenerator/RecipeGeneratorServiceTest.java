package e2.llm.recipegenerator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RecipeGeneratorServiceTest {
    @Autowired
    private RecipeSqlValidator sqlValidator;

    @Autowired
    private RecipeGeneratorService sqlGeneratorService;

    @Test
    void testSQLValidatorWithValidSQL() {
        String validSQL = "CREATE TABLE test (id INT PRIMARY KEY);";
        boolean isValid = sqlValidator.isValidSql(validSQL);
        assertTrue(isValid, "Valid SQL should pass validation");
    }

    @Test
    void testSQLValidatorWithInvalidSQL() {
        String invalidSQL = "CREATED TABLE tableron";
        boolean isValid = sqlValidator.isValidSql(invalidSQL);
        assertFalse(isValid, "Invalid SQL should fail validation");
    }

    @Test
    void testGenerateAndValidateAlterTableSQL() {
        String schemaJson = """
                {
                  "tableName": "employees",
                  "columns": [
                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
                    {"name": "name", "type": "VARCHAR(100)", "constraints": ["NOT NULL"]}
                  ]
                }
                """;
        String userRequest = "Add a new column called email of type VARCHAR(255) that is unique.";
        String expectedSQL = "ALTER TABLE employees ADD COLUMN email VARCHAR(255) UNIQUE;";

        String generatedSQL = sqlGeneratorService.generateSQLQuery(schemaJson, userRequest);
        System.out.println("Generated SQL: " + generatedSQL);

        // SQL validation
        assertTrue(sqlValidator.isValidSql(generatedSQL),
                "Generated SQL should be valid. Error: " + sqlValidator.getValidationError(generatedSQL));
        // Check if the generated SQL matches the expected SQL
        assertEquals(expectedSQL, generatedSQL);
    }

    @Test
    void testGenerateAndValidateComplexComputedColumn() {
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

        String generatedSQL = sqlGeneratorService.generateSQLQuery(schemaJson, userRequest);
        System.out.println("Generated SQL: " + generatedSQL);
        System.out.println("Expected SQL: " + expectedSQL);

        // SQL validation
        assertTrue(sqlValidator.isValidSql(generatedSQL),
                "Generated SQL should be valid. Error: " + sqlValidator.getValidationError(generatedSQL));

        // Check if the generated SQL starts with the expected SQL or vice versa
        assertTrue(generatedSQL.startsWith(expectedSQL) || expectedSQL.startsWith(generatedSQL));
    }
}
