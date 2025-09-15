package com.darkbladedev.testing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test para verificar que SQLite funciona correctamente sin problemas de UnsatisfiedLinkError
 */
public class SQLiteConnectionTest {
    
    private Path tempDbPath;
    private Connection connection;
    
    @BeforeEach
    void setUp() throws IOException {
        // Crear un archivo temporal para la base de datos SQLite
        tempDbPath = Files.createTempFile("test_sqlite", ".db");
    }
    
    @AfterEach
    void tearDown() throws SQLException, IOException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        // Limpiar el archivo temporal
        Files.deleteIfExists(tempDbPath);
    }
    
    @Test
    @DisplayName("Verificar que SQLite puede crear conexión sin UnsatisfiedLinkError")
    void testSQLiteConnection() {
        assertDoesNotThrow(() -> {
            String jdbcUrl = "jdbc:sqlite:" + tempDbPath.toString();
            connection = DriverManager.getConnection(jdbcUrl);
            
            assertNotNull(connection, "La conexión SQLite no debe ser null");
            assertFalse(connection.isClosed(), "La conexión SQLite debe estar abierta");
        }, "SQLite debe conectarse sin lanzar UnsatisfiedLinkError");
    }
    
    @Test
    @DisplayName("Verificar operaciones básicas de SQLite")
    void testSQLiteOperations() {
        assertDoesNotThrow(() -> {
            String jdbcUrl = "jdbc:sqlite:" + tempDbPath.toString();
            connection = DriverManager.getConnection(jdbcUrl);
            
            // Crear una tabla de prueba
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE test_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL)");
                
                // Insertar datos de prueba
                stmt.execute("INSERT INTO test_table (name) VALUES ('test_data')");
                
                // Verificar que los datos se insertaron correctamente
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM test_table")) {
                    assertTrue(rs.next(), "Debe haber al menos un resultado");
                    assertEquals(1, rs.getInt("count"), "Debe haber exactamente un registro");
                }
                
                // Verificar que podemos leer los datos
                try (ResultSet rs = stmt.executeQuery("SELECT name FROM test_table WHERE id = 1")) {
                    assertTrue(rs.next(), "Debe encontrar el registro insertado");
                    assertEquals("test_data", rs.getString("name"), "El nombre debe coincidir");
                }
            }
        }, "Las operaciones básicas de SQLite deben funcionar correctamente");
    }
    
    @Test
    @DisplayName("Verificar que SQLite maneja correctamente los tipos de datos")
    void testSQLiteDataTypes() {
        assertDoesNotThrow(() -> {
            String jdbcUrl = "jdbc:sqlite:" + tempDbPath.toString();
            connection = DriverManager.getConnection(jdbcUrl);
            
            try (Statement stmt = connection.createStatement()) {
                // Crear tabla con diferentes tipos de datos
                stmt.execute("CREATE TABLE data_types_test (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "text_field TEXT, " +
                    "integer_field INTEGER, " +
                    "real_field REAL, " +
                    "blob_field BLOB" +
                    ")");
                
                // Insertar datos de diferentes tipos
                stmt.execute("INSERT INTO data_types_test (text_field, integer_field, real_field) " +
                    "VALUES ('texto_prueba', 42, 3.14)");
                
                // Verificar que los datos se almacenaron correctamente
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM data_types_test WHERE id = 1")) {
                    assertTrue(rs.next(), "Debe encontrar el registro");
                    assertEquals("texto_prueba", rs.getString("text_field"));
                    assertEquals(42, rs.getInt("integer_field"));
                    assertEquals(3.14, rs.getDouble("real_field"), 0.001);
                }
            }
        }, "SQLite debe manejar correctamente diferentes tipos de datos");
    }
}