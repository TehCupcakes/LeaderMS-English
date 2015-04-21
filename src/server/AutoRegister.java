package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import client.LoginCrypto;
import net.login.LoginServer;
import database.DatabaseConnection;

public class AutoRegister {

    public static boolean success;

    public static boolean getAccountExists(String login) {
        boolean accountExists = false;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT name FROM accounts WHERE name = ?");
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.first()) {
                accountExists = true;
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            System.out.println("XSource: Error acquiring the account of (" + login + ").");
        }
        return accountExists;
    }

    public static void createAccount(String login, String pwd, String eip) {
        try {
            PreparedStatement ipq = DatabaseConnection.getConnection().prepareStatement("SELECT lastknownip FROM accounts WHERE lastknownip = ?");
            ipq.setString(1, eip.substring(1, eip.lastIndexOf(':')));
            ResultSet rs = ipq.executeQuery();
            if (!rs.first() || rs.last() && rs.getRow() < LoginServer.getInstance().AutoRegLimit()) {
                try {
                    PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO accounts (name, password, email, birthday, macs, lastknownip) VALUES (?, ?, ?, ?, ?, ?)");
                    ps.setString(1, login);
                    ps.setString(2, LoginCrypto.hexSha1(pwd));
                    ps.setString(3, "no@email.provided");
                    ps.setString(4, "0000-00-00");
                    ps.setString(5, "00-00-00-00-00-00");
                    ps.setString(6, eip.substring(1, eip.lastIndexOf(':')));
                    ps.executeUpdate();
                    ps.close();
                    success = true;
                } catch (Exception ex) {
                    System.out.println("XSource: Error creating the account of (" + login + " | " + pwd + " | " + eip + ").");
                }
            }
            rs.close();
            ipq.close();
        } catch (Exception ex) {
            System.out.println("XSource: Error creating " + login + "'s account.");
        }
    }
}