package com.ruoyi.system.mapper;

import java.util.List;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.AiSuggestWord;

/**
 * AI搜索推荐词 数据层
 *
 * @author ruoyi
 */
public interface AiSuggestWordMapper extends BaseMapper<AiSuggestWord>
{
    /**
     * 查询所有启用的推荐词
     *
     * @return 推荐词集合
     */
    List<AiSuggestWord> selectActiveWords();
}
