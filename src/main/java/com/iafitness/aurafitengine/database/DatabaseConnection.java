package com.iafitness.aurafitengine.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Ajuste aqui a porta e o nome correto do seu schema do MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/ia_fitness";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Substitua pela sua senha local do MySQL

    public static Connection getConnection() throws SQLException {
        try {
            // Força o registro do driver por reflexão para contornar o isolamento de Classpath do JavaFX
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("Erro crítico: O Driver clássico do MySQL não foi localizado.");
            throw new SQLException(e);
        }
    }
}