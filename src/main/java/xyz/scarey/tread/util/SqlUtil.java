package xyz.scarey.tread.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqlUtil {
    public static int getLastIntRow(Connection conn, String table, String column) throws SQLException {
        Statement statement = conn.createStatement();
        int result = 0;

        String SQL = String.format("SELECT * from %s ORDER BY %s DESC LIMIT 1", table, column);
        ResultSet rs = statement.executeQuery(SQL);
        while(rs.next()) {
            result = rs.getInt(column);
        }

        return result;
    }
}
