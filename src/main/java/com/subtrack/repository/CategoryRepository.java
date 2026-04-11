package com.subtrack.repository;

import com.subtrack.config.DatabaseConfig;
import com.subtrack.domain.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a dados para entidades de categoria.
 */
public class CategoryRepository {

    public List<Category> findAllByUserId(String userId) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM categories WHERE user_id = ? ORDER BY is_default DESC, name ASC";
        List<Category> list = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categorias", e);
        }
        return list;
    }

    public Optional<Category> findById(String id) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM categories WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categoria por id", e);
        }
        return Optional.empty();
    }

    public Optional<Category> findByUserIdAndName(String userId, String name) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM categories WHERE user_id = ? AND LOWER(name) = LOWER(?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categoria por nome", e);
        }
        return Optional.empty();
    }

    public Optional<Category> findDefaultByUserId(String userId) {
        String sql = "SELECT id, user_id, name, color_hex, is_default FROM categories WHERE user_id = ? AND is_default = 1";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar categoria padrão", e);
        }
        return Optional.empty();
    }

    public void create(Category category) {
        String sql = "INSERT INTO categories (id, user_id, name, color_hex, is_default) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, category.getId());
            ps.setString(2, category.getUserId());
            ps.setString(3, category.getName());
            ps.setString(4, category.getColorHex());
            ps.setInt(5, category.isDefault() ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar categoria", e);
        }
    }

    public void update(Category category) {
        String sql = "UPDATE categories SET name = ?, color_hex = ? WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getColorHex());
            ps.setString(3, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar categoria", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = DatabaseConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar categoria", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getString("id"));
        c.setUserId(rs.getString("user_id"));
        c.setName(rs.getString("name"));
        c.setColorHex(rs.getString("color_hex"));
        c.setDefault(rs.getInt("is_default") == 1);
        return c;
    }
}
