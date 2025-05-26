package e2.llm.recipegenerator;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

@Component
public class RecipeSqlValidator {

    private final DataSource dataSource;

    public RecipeSqlValidator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Validates if the given SQL statement is syntactically correct
     * by attempting to prepare it (without executing).
     * @param sql The SQL statement to validate
     * @return true if valid, false otherwise
     */
    public boolean isValid(String sql) {
        try (Connection conn = dataSource.getConnection()) {
            // This will validate the SQL syntax without executing it
            conn.prepareStatement(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isValidSql(String sql) {
        try {
            Statement parsed = CCJSqlParserUtil.parse(sql);
            return parsed != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validates if the SQL is valid and returns detailed error message if not.
     * @param sql The SQL statement to validate
     * @return null if valid, error message otherwise
     */
    public String getValidationError(String sql) {
        try (Connection conn = dataSource.getConnection()) {
            // This will validate the SQL syntax without executing it
            conn.prepareStatement(sql);
            return null; // No error
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}