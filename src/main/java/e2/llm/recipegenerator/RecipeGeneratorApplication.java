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
        //  1. Add bedrock
        //  2. Add csv PREVIEW along with database schema to reduce questions - DONE
        //  3. Add more simple tests to see if the LLM can generate the SQL queries correctly
        //  4. Add UI for presentation
    }

    /** TODO: add those test cases and see how the LLM performs
     1. User Request: "Add a new generated column called subtotal of type DECIMAL(12,2) that is simply the product_price multiplied by quantity."
     Expected SQL: ALTER TABLE sales ADD COLUMN subtotal DECIMAL(12,2) GENERATED ALWAYS AS (product_price * quantity) STORED;

     2. User Request: "Create a generated column total_tax_amount of type DECIMAL(10,2) which is calculated as product_price times quantity times tax_rate."
     Expected SQL: ALTER TABLE sales ADD COLUMN total_tax_amount DECIMAL(10,2) GENERATED ALWAYS AS (product_price * quantity * tax_rate) STORED;

     3. User Request (with typo): "Define a new column final_price (DECIMAL(15,2)) as a generated column that is product_price times quantity plus (product_price times quantity times tax_rat)."
     Expected SQL: ALTER TABLE sales ADD COLUMN final_price DECIMAL(15,2) GENERATED ALWAYS AS ((product_price * quantity) + (product_price * quantity * tax_rate)) STORED;

     4. User Request: "Add a generated column discounted_price_per_unit of type DECIMAL(10,2) calculated as product_price multiplied by (1 minus discount_percentage), assuming 0 for discount_percentage if it's null."
     Expected SQL: ALTER TABLE sales ADD COLUMN discounted_price_per_unit DECIMAL(10,2) GENERATED ALWAYS AS (product_price * (1 - COALESCE(discount_percentage, 0.00))) STORED;

     5. User Request: "Make a new generated column net_total_after_discount (DECIMAL(12,2)) which is product_price times quantity times (1 minus COALESCE(discount_percentage, 0.00))."
     Expected SQL: ALTER TABLE sales ADD COLUMN net_total_after_discount DECIMAL(12,2) GENERATED ALWAYS AS (product_price * quantity * (1 - COALESCE(discount_percentage, 0.00))) STORED;

     6. User Request: "Add a generated column order_value_with_all_adjustments of type DECIMAL(15,2) that takes product_price times quantity, applies the discount_percentage (defaulting to 0 if null), and then adds tax based on this discounted subtotal and the tax_rate."
     Expected SQL: ALTER TABLE sales ADD COLUMN order_value_with_all_adjustments DECIMAL(15,2) GENERATED ALWAYS AS ((product_price * quantity * (1 - COALESCE(discount_percentage, 0.00))) * (1 + tax_rate)) STORED;

     7. User Request: "Create a generated is_large_order column (BOOLEAN or INT) that is true (or 1) if quantity is greater than 10, and false (or 0) otherwise."
     Expected SQL (using INT for broader compatibility): ALTER TABLE sales ADD COLUMN is_large_order INT GENERATED ALWAYS AS (CASE WHEN quantity > 10 THEN 1 ELSE 0 END) STORED;
     (For PostgreSQL, BOOLEAN would be: ALTER TABLE sales ADD COLUMN is_large_order BOOLEAN GENERATED ALWAYS AS (quantity > 10) STORED;)

     8. User Request (with typo): "Add a generated column is_priority_electronic as an INT type, which is 1 if LOB is 'Electronics' AND quantity is greater then 1, and 0 otherwise."
     Expected SQL: ALTER TABLE sales ADD COLUMN is_priority_electronic INT GENERATED ALWAYS AS (CASE WHEN LOB = 'Electronics' AND quantity > 1 THEN 1 ELSE 0 END) STORED;

     9. User Request: "Define a new generated column delivery_target_month of type VARCHAR(7) representing the year and month of the delivery_date formatted as 'YYYY-MM'; it should be NULL if delivery_date is NULL."
     Expected SQL (PostgreSQL/SQLite example): ALTER TABLE sales ADD COLUMN delivery_target_month VARCHAR(7) GENERATED ALWAYS AS (CASE WHEN delivery_date IS NOT NULL THEN strftime('%Y-%m', delivery_date) ELSE NULL END) STORED;
     (MySQL: FORMAT_DATE(delivery_date, '%Y-%m') or similar, SQL Server: FORMAT(delivery_date, 'yyyy-MM'))

     10. User Request: "Create a generated column status_group of type VARCHAR(50) that is 'Active' if status is 'PENDING' or 'SHIPPED', and 'Finalized' if status is 'COMPLETED' or 'DELIVERED', otherwise 'Other'."
     Expected SQL: ALTER TABLE sales ADD COLUMN status_group VARCHAR(50) GENERATED ALWAYS AS (CASE WHEN status IN ('PENDING', 'SHIPPED') THEN 'Active' WHEN status IN ('COMPLETED', 'DELIVERED') THEN 'Finalized' ELSE 'Other' END) STORED;
     */
}
