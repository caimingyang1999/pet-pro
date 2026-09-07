/**
 * 首页 Mock 数据
 * 后续替换为后端接口
 */

// 统计卡片数据
export const statsData = [
  {
    key: 'orders',
    title: '订单总数',
    value: 1268,
    growth: 12.5,
    iconType: 'order',
    color: '#3b82f6',
    bgGradient: 'linear-gradient(135deg, #dbeafe 0%, #eff6ff 100%)',
    trendData: [120, 132, 101, 134, 90, 230, 210]
  },
  {
    key: 'products',
    title: '商品总数',
    value: 326,
    growth: 8.2,
    iconType: 'product',
    color: '#10b981',
    bgGradient: 'linear-gradient(135deg, #d1fae5 0%, #ecfdf5 100%)',
    trendData: [80, 92, 101, 94, 120, 130, 145]
  },
  {
    key: 'activities',
    title: '宠物动态总数',
    value: 2856,
    growth: 15.7,
    iconType: 'activity',
    color: '#8b5cf6',
    bgGradient: 'linear-gradient(135deg, #ede9fe 0%, #f5f3ff 100%)',
    trendData: [220, 182, 191, 234, 290, 330, 345]
  },
  {
    key: 'pets',
    title: '宠物总数',
    value: 1024,
    growth: 9.3,
    iconType: 'pet',
    color: '#f97316',
    bgGradient: 'linear-gradient(135deg, #fed7aa 0%, #fff7ed 100%)',
    trendData: [100, 112, 101, 114, 130, 140, 155]
  }
]

// 数据趋势 - 近7天
export const dataTrendData = {
  dates: ['05-14', '05-15', '05-16', '05-17', '05-18', '05-19', '05-20'],
  series: [
    { name: '订单数量', color: '#3b82f6', data: [820, 932, 901, 934, 1290, 1330, 1320] },
    { name: '商品数量', color: '#10b981', data: [220, 382, 401, 534, 690, 730, 820] },
    { name: '动态数量', color: '#8b5cf6', data: [520, 632, 701, 834, 890, 930, 980] },
    { name: '宠物数量', color: '#f97316', data: [320, 332, 401, 434, 590, 630, 680] }
  ],
  tooltipDetail: {
    date: '05-17',
    '订单数量': 1023,
    '商品数量': 334,
    '动态数量': 2231,
    '宠物数量': 986
  }
}

// 订单状态分布
export const orderStatusData = {
  total: 1268,
  totalLabel: '订单总数',
  legend: [
    { name: '待发货', value: 386, percent: 30.44, color: '#3b82f6' },
    { name: '已发货', value: 425, percent: 33.51, color: '#10b981' },
    { name: '已完成', value: 324, percent: 25.55, color: '#8b5cf6' },
    { name: '已取消', value: 129, percent: 10.19, color: '#f97316' }
  ]
}

// 待处理事项
export const pendingTasksData = [
  { id: 1, title: '待审核动态', count: 18, iconType: 'review', color: '#3b82f6' },
  { id: 2, title: '待发货订单', count: 36, iconType: 'ship', color: '#10b981' },
  { id: 3, title: '库存预警商品', count: 12, iconType: 'warning', color: '#f97316' },
  { id: 4, title: '用户投诉', count: 5, iconType: 'complaint', color: '#ef4444' }
]

// 最新动态
export const latestActivitiesData = [
  {
    id: 1,
    user: '张三',
    avatar: '',
    content: '今天带猫咪去洗澡啦，超级乖~',
    time: '2分钟前',
    status: '待审核',
    statusType: 'pending'
  },
  {
    id: 2,
    user: '李四',
    avatar: '',
    content: '狗狗的新玩具到了，好开心！',
    time: '15分钟前',
    status: '已通过',
    statusType: 'success'
  },
  {
    id: 3,
    user: '王五',
    avatar: '',
    content: '求推荐适合小型犬的狗粮~',
    time: '30分钟前',
    status: '待审核',
    statusType: 'pending'
  },
  {
    id: 4,
    user: '赵六',
    avatar: '',
    content: '猫咪绝育记录分享',
    time: '1小时前',
    status: '已通过',
    statusType: 'success'
  }
]

// 热门商品 TOP5
export const hotProductsData = [
  { rank: 1, name: '冻干鸡肉粒 200g', exchanges: 326, percent: 32, image: '' },
  { rank: 2, name: '宠物除味喷雾 500ml', exchanges: 277, percent: 27, image: '' },
  { rank: 3, name: '互动逗猫玩具球', exchanges: 245, percent: 24, image: '' },
  { rank: 4, name: '宠物围巾（多色）', exchanges: 198, percent: 19, image: '' },
  { rank: 5, name: '宠物磨牙棒 10支装', exchanges: 176, percent: 17, image: '' }
]

// 用户增长趋势
export const userGrowthData = {
  dates: ['05-14', '05-15', '05-16', '05-17', '05-18', '05-19', '05-20'],
  data: [120, 182, 241, 234, 320, 410, 480]
}
