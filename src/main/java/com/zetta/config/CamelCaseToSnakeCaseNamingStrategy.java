package com.zetta.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Custom Hibernate naming strategy to convert Java camelCase to database snake_case.
 * This ensures fields without explicit @Column annotations are automatically converted.
 */
public class CamelCaseToSnakeCaseNamingStrategy implements PhysicalNamingStrategy {
    
    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }
    
    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }
    
    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }
    
    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        return name;
    }
    
    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
        // Convert camelCase to snake_case
        String text = name.getText();
        String snakeCase = convertCamelToSnake(text);
        return Identifier.toIdentifier(snakeCase, name.isQuoted());
    }
    
    /**
     * Converts camelCase to snake_case.
     * Example: "userId" -> "user_id", "targetFriendId" -> "target_friend_id"
     */
    private String convertCamelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}

