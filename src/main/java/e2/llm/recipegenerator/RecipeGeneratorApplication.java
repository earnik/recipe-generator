package e2.llm.recipegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class RecipeGeneratorApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(RecipeGeneratorApplication.class, args);

        RecipeGenerationChatMemoryService recipeGenerationChatMemoryService = context.getBean(RecipeGenerationChatMemoryService.class);
// Test 1
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

        // Test 2
//                String schemaJson = """
//                {
//                  "tableName": "sales",
//                  "columns": [
//                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
//                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
//                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
//                    {"name": "tax", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]},
//                    {"name": "percentage", "type": "DECIMAL(3,2)", "constraints": ["NOT NULL"]}
//                  ]
//                }
//                """;
//        String userRequest = "Add a new computed column called total_with_tax of type DECIMAL(20,2) that multiplies product_price by percentage and then adds tax";

        // Test 3
//        String schemaJson = """
//                {
//                  "tableName": "sales",
//                  "columns": [
//                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
//                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
//                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
//                    {"name": "tax", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]},
//                    {"name": "storage_type", "type": "VARCHAR(255)"},
//                    {"name": "delivery_date", "type": "DATE"},
//                    {"name": "customer_id", "type": "INT", "constraints": ["NOT NULL"]},
//                    {"name": "LOB", "type": "VARCHAR(255)"},
//                    {"name": "status", "type": "VARCHAR(50)", "constraints": ["NOT NULL"]},
//                    {"name": "price_rate", "type": "DECIMAL(3,2)}
//                  ]
//                }
//                """;
//        String userRequest = "Add a new computed column called magic_formula of type DECIMAL(20,2) that multiplies product_price by tax, then adds price_rate divided by quantity and add 5.67 to the result.";

        // Test 3
        String schemaJson = """
                {
                  "tableName": "sales",
                  "columns": [
                    {"name": "id", "type": "INT", "constraints": ["PRIMARY KEY"]},
                    {"name": "product_price", "type": "DECIMAL(10,2)", "constraints": ["NOT NULL"]},
                    {"name": "quantity", "type": "INT", "constraints": ["NOT NULL"]},
                    {"name": "tax", "type": "DECIMAL(5,2)", "constraints": ["NOT NULL"]},
                    {"name": "storage_type", "type": "VARCHAR(255)"},
                    {"name": "delivery_date", "type": "DATE"},
                    {"name": "customer_id", "type": "INT", "constraints": ["NOT NULL"]},
                    {"name": "LOB", "type": "VARCHAR(255)"},
                    {"name": "status", "type": "VARCHAR(50)", "constraints": ["NOT NULL"]},
                    {"name": "price_rate", "type": "DECIMAL(3,2)}
                  ]
                }
                """;
        String userRequest = "final_price_after_discount (DECIMAL(12,2)): This should be the product_price multiplied by quantity, and then the discount_percentage should be applied. If discount_percentage is NULL, assume a 0% discount.\n" +
                "total_amount_payable (DECIMAL(12,2)): This is the final_price_after_discount plus the tax.\n" +
                "order_category (VARCHAR(100)):\n" +
                "If the LOB is 'Electronics' AND product_price is greater than 1500, it should be 'High-Value Electronics'.\n" +
                "If the LOB is 'Books' AND quantity is greater than 10, it should be 'Bulk Books Order'.\n" +
                "If the status is 'CANCELLED', it should be 'Cancelled Order'.\n" +
                "For all other orders, if priority_level is 1, it should be 'Priority Order - General'.\n" +
                "Otherwise, it should be 'Standard Order'.\n" +
                "estimated_shipping_tier (VARCHAR(50)):\n" +
                "If delivery_date is NULL:\n" +
                "If status is 'PENDING' and order_date is older than 7 days from today (assume today is '2024-03-15'), set this to 'Delayed - Action Required'.\n" +
                "Otherwise (if delivery_date is NULL but not meeting the above pending condition), set it to 'Awaiting Shipment Info'.\n" +
                "If delivery_date is NOT NULL:\n" +
                "Calculate the difference in days between delivery_date and order_date.\n" +
                "If the difference is 0 or 1 day, set to 'Express'.\n" +
                "If the difference is between 2 and 5 days (inclusive), set to 'Standard'.\n" +
                "If the difference is greater than 5 days, set to 'Economy'.\n" +
                "If delivery_date is before order_date, set it to 'Data Error - Delivery Before Order'.\n" +
                "Make sure all calculated monetary values are rounded to 2 decimal places.\"";


        recipeGenerationChatMemoryService.initializeConversation(schemaJson, RecipeGenerationChatMemoryService.INSTRUCTIONS, "sales_preview.csv");

//        recipeGenerationChatMemoryService.initializeConversation(schemaJson, RecipeGenerationChatMemoryService.SMART_INSTRUCTIONS);

        String response = recipeGenerationChatMemoryService.testConversation(userRequest);
        System.out.println("LLM FINAL RESPONSE: " + response);


        // TODO: next steps:
        //  1.  Add bedrock
        //  2.  Add csv PREVIEW along with database schema to reduce questions - DONE
        //  3.  Add UI for presentation
    }
}
