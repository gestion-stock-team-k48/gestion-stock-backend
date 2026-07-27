package cm.kfokam.stock.mvtstk;

import cm.kfokam.stock.mvtstk.dto.MvtStkRequest;
import cm.kfokam.stock.mvtstk.dto.MvtStkResponse;
import cm.kfokam.stock.mvtstk.model.MvtStk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface MvtStkMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dateMvt", ignore = true)
    @Mapping(target = "typeMvt", ignore = true)
    @Mapping(target = "article", ignore = true)
    MvtStk toEntity(MvtStkRequest request);

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleDesignation", source = "article.designation")
    MvtStkResponse toResponse(MvtStk mvtStk);

    List<MvtStkResponse> toResponseList(List<MvtStk> mvtStks);
}