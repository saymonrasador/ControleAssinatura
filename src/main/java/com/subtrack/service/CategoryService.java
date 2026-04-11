package com.subtrack.service;

import com.subtrack.domain.Category;
import com.subtrack.repository.CategoryRepository;
import com.subtrack.repository.SubscriptionRepository;
import com.subtrack.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Lógica de negócios para gerenciamento de categorias.
 */
public class CategoryService {

    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final SubscriptionRepository subscriptionRepository = new SubscriptionRepository();

    public List<Category> getAllByUserId(String userId) {
        return categoryRepository.findAllByUserId(userId);
    }

    public Optional<Category> getById(String id) {
        return categoryRepository.findById(id);
    }

    /**
     * Cria uma nova categoria para o usuário.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String create(String userId, String name, String colorHex) {
        String error = ValidationUtil.validateNameAndColor(name, colorHex);
        if (error != null)
            return error;

        // Verifica se a categoria já existe
        if (categoryRepository.findByUserIdAndName(userId, name.trim()).isPresent()) {
            return "Já existe uma categoria com este nome.";
        }

        Category category = new Category();
        category.setId(UUID.randomUUID().toString());
        category.setUserId(userId);
        category.setName(name.trim());
        category.setColorHex(colorHex != null && !colorHex.trim().isEmpty() ? colorHex.trim() : null);
        category.setDefault(false);
        categoryRepository.create(category);
        return null;
    }

    /**
     * Atualiza uma categoria existente.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String update(String categoryId, String userId, String name, String colorHex) {
        String error = ValidationUtil.validateNameAndColor(name, colorHex);
        if (error != null)
            return error;

        // Verifica se a categoria já existe
        Optional<Category> existing = categoryRepository.findByUserIdAndName(userId, name.trim());
        if (existing.isPresent() && !existing.get().getId().equals(categoryId)) {
            return "Já existe uma categoria com este nome.";
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        category.setName(name.trim());
        category.setColorHex(colorHex != null && !colorHex.trim().isEmpty() ? colorHex.trim() : null);
        categoryRepository.update(category);
        return null;
    }

    /**
     * Deleta uma categoria. As assinaturas vinculadas a ela são realocadas para a
     * categoria padrão.
     * 
     * @return mensagem de erro ou null em caso de sucesso
     */
    public String delete(String categoryId, String userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (category.isDefault()) {
            return "Não é possível deletar a categoria padrão.";
        }

        // Realoca as assinaturas para a categoria padrão
        Category defaultCategory = categoryRepository.findDefaultByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Nenhuma categoria padrão encontrada"));
        subscriptionRepository.reassignCategory(categoryId, defaultCategory.getId());

        categoryRepository.delete(categoryId);
        return null;
    }
}
