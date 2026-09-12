import request from '@/utils/request'

/**
 * 分页查询养宠知识文章列表（后台，含草稿）
 * @param {Object} query - 查询参数
 * @param {number} query.pageNum - 页码
 * @param {number} query.pageSize - 每页条数
 * @param {string} [query.title] - 标题（模糊搜索）
 * @param {string} [query.category] - 分类
 * @param {string} [query.status] - 状态（0-草稿 1-已发布）
 * @returns {Promise<Object>} 文章列表数据
 */
export function getArticleList(query) {
  return request({
    url: '/api/v1/admin/articles/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取文章详情（含草稿正文）
 * @param {string|number} id - 文章ID
 * @returns {Promise<Object>} 文章详情数据
 */
export function getArticleDetail(id) {
  return request({
    url: '/api/v1/admin/articles/' + id,
    method: 'get'
  })
}

/**
 * 新增文章
 * @param {Object} data - 文章数据
 * @param {string} data.title - 标题（必填）
 * @param {string} [data.summary] - 摘要（列表页展示）
 * @param {string} [data.coverImage] - 封面图地址（通过公共上传接口获取）
 * @param {string} [data.content] - 正文（富文本 HTML）
 * @param {string} [data.category] - 分类
 * @param {string} [data.tags] - 标签（逗号分隔）
 * @param {string} [data.source] - 来源
 * @param {number} [data.sortOrder] - 排序号（数字越小越靠前）
 * @param {string} [data.status] - 状态（0-草稿 1-已发布，默认1）
 * @returns {Promise<Object>} 新增结果
 */
export function addArticle(data) {
  return request({
    url: '/api/v1/admin/articles',
    method: 'post',
    data: data
  })
}

/**
 * 编辑文章
 * @param {Object} data - 文章数据
 * @param {number} data.id - 文章ID
 * @returns {Promise<Object>} 编辑结果
 */
export function updateArticle(data) {
  return request({
    url: '/api/v1/admin/articles/' + data.id,
    method: 'put',
    data: data
  })
}

/**
 * 修改文章发布状态（列表页快捷切换草稿/已发布）
 * @param {string|number} id - 文章ID
 * @param {string} status - 状态（0-草稿 1-已发布）
 * @returns {Promise<Object>} 修改结果
 */
export function changeArticleStatus(id, status) {
  return request({
    url: '/api/v1/admin/articles/' + id + '/status',
    method: 'put',
    params: { status: status }
  })
}

/**
 * 删除文章（逻辑删除）
 * @param {string|number} id - 文章ID
 * @returns {Promise<Object>} 删除结果
 */
export function deleteArticle(id) {
  return request({
    url: '/api/v1/admin/articles/' + id,
    method: 'delete'
  })
}

/**
 * AI 生成一篇养宠知识文章（落库为草稿，需人工审核后发布）
 * @param {Object} params - 生成参数
 * @param {string} [params.petType] - 适用宠物类型（general/cat/dog/rabbit/bird/fish）
 * @param {string} [params.category] - 主题分类（喂养/健康/训练/洗护/疾病）
 * @param {string} [params.topic] - 指定选题（留空由模型自拟）
 * @param {boolean} [params.withImage] - 是否生成配图
 * @param {number} [params.inlineCount] - 正文内嵌配图数量（1~3）
 * @returns {Promise<Object>} 生成的草稿文章（含 id、coverImage、content 等）
 */
export function generateArticle(params) {
  return request({
    url: '/api/v1/admin/articles/generate',
    method: 'post',
    data: params,
    // 文章 + 配图生成耗时较长，放宽超时
    timeout: 120000
  })
}

/**
 * AI 文章生成能力探测（是否已配置 AI 配图、所用模型等）
 * @returns {Promise<Object>} { available, aiImageEnabled, imageModel, articleModel }
 */
export function getArticleGenerateStatus() {
  return request({
    url: '/api/v1/admin/articles/generate/status',
    method: 'get'
  })
}
