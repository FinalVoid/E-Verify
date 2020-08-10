package net.everify.sql;



import net.everify.Constant;
import net.everify.EVerify;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;
    private String host, dbName, user, password;
    private int port;

    public DatabaseManager(String host, int port, String dbName, String user, String password) {

        this.host = host;
        this.dbName = dbName;
        this.user = user;
        this.password = password;
        this.port = port;

    }

    public void openConnection() throws SQLException {

        if(connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if(connection != null && !connection.isClosed()) {
                return;
            }
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.dbName,
                    this.user, this.password);
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS  `mails` " +
                    "(`id` binary(16) NOT NULL DEFAULT '', " +
                    "`mail` char(36) NOT NULL DEFAULT '', " +
                    "`mdomain` char(36) NOT NULL DEFAULT 'gmail.com', " +
                    "`code` int(4) NOT NULL DEFAULT '0000'," +
                    "PRIMARY KEY (`id`)); ");
        }

    }

    public Connection getConnection() {
        return connection;
    }

    public void insertEmail(UUID id, String mail, int code) {

        String name = mail.split("@")[0];
        String domain = mail.split("@")[1];

        byte[] b = Constant.idToBytes(id);

        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO mails (id, mail, mdomain, code) VALUES (?, ?, ?, ?)");
                    statement.setObject(1, b);
                    statement.setObject(2, name);
                    statement.setObject(3, domain);
                    statement.setObject(4, code);

                    statement.executeUpdate();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(EVerify.getInstance());
    }

    public Object[] getPlayerInformation(UUID id) {

        String query = "SELECT * FROM id = '" + Constant.idToBytes(id) + "'";
        final Object[][] output = new Object[1][1];

        new BukkitRunnable() {
            @Override
            public void run() {

                try {

                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);

                    if(resultSet.wasNull()) {
                        Object[] obj = {false};
                        output[0] = obj;
                    } else {
                        output[0] = new Object[]{true, resultSet.getString("mail") + "@" + resultSet.getString("domain"), resultSet.getInt("code")};
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }.runTaskAsynchronously(EVerify.getInstance());

        return output;

    }



}
