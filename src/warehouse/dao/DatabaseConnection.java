package warehouse.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private DatabaseConnection() {}

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC driver nuk u gjet.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.getUrl(),
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
    }

    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            if (!conn.isValid(5)) {
                throw new SQLException("Lidhja me databazën dështoi.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("UnknownHost") || msg.contains("ENOTFOUND") && msg.contains("db.")) {
                throw new SQLException(
                        "Hosti direkt db.*.supabase.co nuk arrihet (zakonisht IPv6). "
                                + "Përdorni Session Pooler nga Supabase Dashboard → Connect "
                                + "(db.user=postgres.<project-ref>, host=aws-0-REGION.pooler.supabase.com).",
                        e);
            }
            throw e;
        }
    }
}
