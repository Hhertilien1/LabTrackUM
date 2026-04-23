package com.um.labtrack.config;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import java.sql.SQLException;

/**
 * Shows a short, actionable message when MySQL connection fails (e.g. server not running
 * or "Public Key Retrieval is not allowed") instead of a long stack trace.
 * Uses only java.sql types so the MySQL driver can stay as runtime scope.
 */
public class MySQLConnectionFailureAnalyzer extends AbstractFailureAnalyzer<SQLException> {

    private static String getRootMessage(Throwable t) {
        while (t != null && t.getCause() != null) t = t.getCause();
        return t != null ? t.getMessage() : "";
    }

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, SQLException cause) {
        String msg = getRootMessage(cause);
        if (msg.contains("Public Key Retrieval")) {
            return new FailureAnalysis(
                "MySQL connection failed: Public Key Retrieval is not allowed.",
                "Add allowPublicKeyRetrieval=true to the JDBC URL in application.properties.",
                cause
            );
        }
        if (msg.contains("Communications link failure") || cause.getClass().getName().contains("Communications")) {
            return new FailureAnalysis(
                "Could not connect to MySQL. The server may be stopped or unreachable.",
                "Start MySQL and check URL/username/password in application.properties (default: localhost:3306, user root).",
                cause
            );
        }
        // Only handle connection-related errors; let others use default reporting
        if (msg.contains("link") || msg.contains("connection") || msg.contains("Connection")) {
            return new FailureAnalysis(
                "MySQL connection failed: " + (msg.isEmpty() ? cause.getMessage() : msg),
                "Check MySQL is running and application.properties (URL, username, password).",
                cause
            );
        }
        return null;
    }
}
