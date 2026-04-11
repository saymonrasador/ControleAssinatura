package com.subtrack.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Gerencia o ciclo de vida da conexão com o banco de dados SQLite.
 * Cria o diretório do banco de dados e o esquema na primeira utilização.
 */
public class DatabaseConfig {

    private static final String DB_DIR = System.getProperty("user.home") + "/.subtrack";
    private static final String DB_FILE = DB_DIR + "/subtrack.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection;

    private DatabaseConfig() {
    }

    /**
     * Retorna uma conexão com o banco de dados compartilhada. Cria o banco de dados
     * e o esquema se necessário.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            ensureDirectoryExists();
            connection = DriverManager.getConnection(DB_URL);
            // Habilita a aplicação de chaves estrangeiras no SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema();
        }
        return connection;
    }

    /**
     * Cria o diretório do banco de dados se ele não existir.
     */
    private static void ensureDirectoryExists() {
        try {
            Path dir = Paths.get(DB_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar o diretório do banco de dados: " + DB_DIR, e);
        }
    }

    /**
     * Lê e executa o arquivo schema.sql para criar as tabelas.
     */
    private static void initializeSchema() throws SQLException {
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/schema.sql")) {
            if (is == null) {
                throw new RuntimeException("schema.sql não encontrado nos recursos");
            }
            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Divide por ponto e vírgula e executa cada instrução
            String[] statements = sql.split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String s : statements) {
                    String trimmed = s.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler schema.sql", e);
        }
    }

    /**
     * Fecha a conexão com o banco de dados compartilhada.
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão com o banco de dados: " + e.getMessage());
            }
            connection = null;
        }
    }
}
