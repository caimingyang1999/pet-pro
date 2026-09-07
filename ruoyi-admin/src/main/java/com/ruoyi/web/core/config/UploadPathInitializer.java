package com.ruoyi.web.core.config;

import java.io.File;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.StringUtils;

/**
 * 上传目录自动初始化
 *
 * 说明：
 * 1. 应用启动时自动创建 ruoyi.profile 指向的根目录及常用子目录（upload / avatar / import / download），
 *    避免首次部署时因目录不存在导致文件上传失败；
 * 2. 同时在启动日志中打印实际生效路径，便于排查"图片上传成功但看不到"类问题；
 * 3. 该类不修改任何 RuoYi 框架核心逻辑，仅作为启动期的自检增强。
 *
 * @author ruoyi
 */
@Component
public class UploadPathInitializer
{
    private static final Logger log = LoggerFactory.getLogger(UploadPathInitializer.class);

    /** 需要预创建的子目录（与 RuoYiConfig 中 getXxxPath 保持一致） */
    private static final String[] SUB_DIRS = { "upload", "avatar", "import", "download" };

    @PostConstruct
    public void init()
    {
        String profile = RuoYiConfig.getProfile();
        if (StringUtils.isEmpty(profile))
        {
            log.warn("[上传目录初始化] ruoyi.profile 为空，请检查 application.yml 配置！");
            return;
        }
        File root = new File(profile);
        mkdirIfAbsent(root);
        for (String sub : SUB_DIRS)
        {
            mkdirIfAbsent(new File(root, sub));
        }
        log.info("[上传目录初始化] 完成，根目录: {}，可通过 URL 前缀 /profile/**、/dev-api/profile/**、"
                + "/prod-api/profile/**、/api/v1/profile/** 访问", root.getAbsolutePath());
    }

    private void mkdirIfAbsent(File dir)
    {
        if (dir.exists())
        {
            if (!dir.canWrite())
            {
                log.warn("[上传目录初始化] 目录 {} 已存在但无写入权限，请检查运行用户！", dir.getAbsolutePath());
            }
            return;
        }
        if (dir.mkdirs())
        {
            log.info("[上传目录初始化] 已创建目录: {}", dir.getAbsolutePath());
        }
        else
        {
            log.error("[上传目录初始化] 创建目录失败: {}，请检查权限或手动创建！", dir.getAbsolutePath());
        }
    }
}
