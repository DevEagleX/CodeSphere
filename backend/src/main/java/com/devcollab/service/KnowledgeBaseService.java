package com.devcollab.service;

import com.devcollab.dto.*;
import com.devcollab.entity.KbArticle;
import com.devcollab.entity.KbCategory;
import com.devcollab.entity.User;
import com.devcollab.entity.WorkspaceMember;
import com.devcollab.exception.ForbiddenOperationException;
import com.devcollab.exception.ResourceNotFoundException;
import com.devcollab.mapper.KnowledgeBaseMapper;
import com.devcollab.repository.KbArticleRepository;
import com.devcollab.repository.KbCategoryRepository;
import com.devcollab.repository.UserRepository;
import com.devcollab.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KbCategoryRepository categoryRepository;
    private final KbArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final ActivityService activityService;
    private final KnowledgeBaseMapper mapper;

    @Transactional
    public CategoryResponse createCategory(String username, UUID workspaceId, CreateCategoryRequest request) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        KbCategory category = KbCategory.builder()
                .workspace(membership.getWorkspace())
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateUniqueCategorySlug(workspaceId, request.getName()))
                .build();
        categoryRepository.save(category);

        return mapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> listCategories(String username, UUID workspaceId) {
        workspaceService.requireMembership(workspaceId, username);
        return categoryRepository.findByWorkspaceId(workspaceId).stream()
                .map(mapper::toCategoryResponse)
                .toList();
    }

    @Transactional
    public void deleteCategory(String username, UUID workspaceId, UUID categoryId) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        workspaceService.requireRoleAtLeast(membership, WorkspaceMember.WorkspaceRole.ADMIN);

        KbCategory category = categoryRepository.findByIdAndWorkspaceId(categoryId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        List<KbArticle> affected = articleRepository.findByCategoryId(categoryId);
        for (KbArticle article : affected) {
            article.setCategory(null);
        }
        articleRepository.saveAll(affected);

        categoryRepository.delete(category);
    }

    @Transactional
    public ArticleResponse createArticle(String username, UUID workspaceId, CreateArticleRequest request) {
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);
        User author = findUser(username);

        KbCategory category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByIdAndWorkspaceId(request.getCategoryId(), workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
        }

        KbArticle article = KbArticle.builder()
                .workspace(membership.getWorkspace())
                .category(category)
                .author(author)
                .title(request.getTitle())
                .slug(generateUniqueArticleSlug(workspaceId, request.getTitle()))
                .body(request.getBody())
                .published(true)
                .build();
        articleRepository.save(article);

        activityService.log(membership.getWorkspace(), author,
                com.devcollab.entity.ActivityEvent.ActivityType.ARTICLE_CREATED,
                author.getUsername() + " published a KB article: " + article.getTitle());

        return mapper.toArticleResponse(article);
    }

    public PageResponse<ArticleResponse> listArticles(String username, UUID workspaceId, UUID categoryId, Pageable pageable) {
        workspaceService.requireMembership(workspaceId, username);

        Page<KbArticle> page = (categoryId != null)
                ? articleRepository.findByWorkspaceIdAndCategoryIdAndPublishedTrue(workspaceId, categoryId, pageable)
                : articleRepository.findByWorkspaceIdAndPublishedTrue(workspaceId, pageable);

        return PageResponse.from(page, mapper::toArticleResponse);
    }

    public ArticleResponse getArticle(String username, UUID articleId) {
        KbArticle article = findArticle(articleId);
        workspaceService.requireMembership(article.getWorkspace().getId(), username);
        return mapper.toArticleResponse(article);
    }

    @Transactional
    public ArticleResponse updateArticle(String username, UUID articleId, UpdateArticleRequest request) {
        KbArticle article = findArticle(articleId);
        UUID workspaceId = article.getWorkspace().getId();
        workspaceService.requireMembership(workspaceId, username);

        if (!article.getAuthor().getUsername().equals(username)) {
            throw new ForbiddenOperationException("Only the author can edit this article");
        }

        if (request.getCategoryId() != null) {
            KbCategory category = categoryRepository.findByIdAndWorkspaceId(request.getCategoryId(), workspaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.getCategoryId()));
            article.setCategory(category);
        } else {
            article.setCategory(null);
        }

        article.setTitle(request.getTitle());
        article.setBody(request.getBody());
        article.setPublished(request.getPublished());
        articleRepository.save(article);

        return mapper.toArticleResponse(article);
    }

    @Transactional
    public void deleteArticle(String username, UUID articleId) {
        KbArticle article = findArticle(articleId);
        UUID workspaceId = article.getWorkspace().getId();
        WorkspaceMember membership = workspaceService.requireMembership(workspaceId, username);

        boolean isAuthor = article.getAuthor().getUsername().equals(username);
        boolean isAdminOrOwner = membership.getRole() == WorkspaceMember.WorkspaceRole.OWNER
                || membership.getRole() == WorkspaceMember.WorkspaceRole.ADMIN;

        if (!isAuthor && !isAdminOrOwner) {
            throw new ForbiddenOperationException("Only the author or a workspace admin/owner can delete this article");
        }

        articleRepository.delete(article);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private KbArticle findArticle(UUID id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found: " + id));
    }

    private String generateUniqueCategorySlug(UUID workspaceId, String name) {
        String base = SlugUtil.toSlug(name);
        String candidate = base;
        int suffix = 1;
        while (categoryRepository.existsByWorkspaceIdAndSlug(workspaceId, candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private String generateUniqueArticleSlug(UUID workspaceId, String title) {
        String base = SlugUtil.toSlug(title);
        String candidate = base;
        int suffix = 1;
        while (articleRepository.existsByWorkspaceIdAndSlug(workspaceId, candidate)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }
}
