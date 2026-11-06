package com.devcollab.mapper;

import com.devcollab.dto.ArticleResponse;
import com.devcollab.dto.CategoryResponse;
import com.devcollab.entity.KbArticle;
import com.devcollab.entity.KbCategory;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeBaseMapper {

    public CategoryResponse toCategoryResponse(KbCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .workspaceId(category.getWorkspace().getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public ArticleResponse toArticleResponse(KbArticle article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .workspaceId(article.getWorkspace().getId())
                .categoryId(article.getCategory() != null ? article.getCategory().getId() : null)
                .categoryName(article.getCategory() != null ? article.getCategory().getName() : null)
                .authorId(article.getAuthor().getId())
                .authorUsername(article.getAuthor().getUsername())
                .title(article.getTitle())
                .slug(article.getSlug())
                .body(article.getBody())
                .published(article.isPublished())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
