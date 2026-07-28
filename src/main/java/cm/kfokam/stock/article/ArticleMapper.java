package cm.kfokam.stock.article;

import cm.kfokam.stock.article.dto.ArticleRequest;
import cm.kfokam.stock.article.dto.ArticleResponse;
import cm.kfokam.stock.article.model.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
interface ArticleMapper {

    @Mapping(target = "category", ignore = true)
    @Mapping(target = "entreprise", ignore = true)
    Article toEntity(ArticleRequest request);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryDesignation", source = "category.designation")
    ArticleResponse toResponse(Article article);

    List<ArticleResponse> toResponseList(List<Article> articles);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromRequest(ArticleRequest request, @MappingTarget Article article);
}