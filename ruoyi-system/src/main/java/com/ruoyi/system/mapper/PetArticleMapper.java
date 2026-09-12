package com.ruoyi.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.PetArticle;

/**
 * 养宠知识文章 数据层
 *
 * @author ruoyi
 */
public interface PetArticleMapper extends BaseMapper<PetArticle>
{
    /**
     * 小程序端：查询已发布文章列表（支持按宠物类型做兴趣优先排序）
     *
     * 排序规则：与宠物类型一致的文章最前，通用（all/NULL）次之，其它最后；
     * 同一档内仍按 sort_order 升序、ID 降序，保证分页翻页结果稳定不重复。
     *
     * 自定义 SQL 不走 MyBatis-Plus 的逻辑删除，需自行带上 del_flag 条件。
     *
     * @param category 分类（可选）
     * @param keyword  标题/摘要关键词（可选）
     * @param petType  宠物类型（cat/dog，可选；为空时不做兴趣优先，按默认排序）
     * @return 文章集合（不含正文，列表页无需大字段）
     */
    public List<PetArticle> selectPublishedList(@Param("category") String category,
                                                @Param("keyword") String keyword,
                                                @Param("petType") String petType);
}
