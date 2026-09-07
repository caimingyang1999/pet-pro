package com.ruoyi.framework.config;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 预渲染字符验证码 Producer
 * <p>
 * 完全不依赖 AWT Font 渲染，使用 classpath 下预渲染的字符图片合成验证码。
 * 这样在 Linux 服务器无 fontconfig 的环境下也能正常工作。
 * </p>
 *
 * @author ruoyi
 */
public class PreRenderedCaptchaProducer
{
    /** 字符图片缓存（key=字符, value=BufferedImage） */
    private final List<BufferedImage> charImages = new ArrayList<>();
    /** 支持的字符列表 */
    private final List<String> charList = new ArrayList<>();
    private final Random random = new Random();

    /** 验证码图片宽度 */
    private int width = 160;
    /** 验证码图片高度 */
    private int height = 60;
    /** 字符间距 */
    private int charSpacing = 6;
    /** 字符长度 */
    private int charLength = 4;
    /** 支持的额外字符（运算符等） */
    private String extraChars = "+-x÷=?";

    /**
     * 从 classpath 加载预渲染字符图片
     */
    public void init()
    {
        // 加载基础字符 0-9, A-Z（不含 I/O）
        String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ";
        for (int i = 0; i < chars.length(); i++)
        {
            String ch = String.valueOf(chars.charAt(i));
            loadChar(ch);
        }
        // 加载额外字符（运算符）
        for (int i = 0; i < extraChars.length(); i++)
        {
            String ch = String.valueOf(extraChars.charAt(i));
            loadChar(ch);
        }
    }

    /**
     * 加载单个字符图片
     */
    private void loadChar(String ch)
    {
        // 特殊字符映射到文件名（+ -> plus, - -> minus 等）
        String fileName = getFileName(ch);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("captcha/chars/" + fileName + ".png"))
        {
            if (is != null)
            {
                BufferedImage img = ImageIO.read(is);
                charImages.add(img);
                charList.add(ch);
            }
        }
        catch (Exception e)
        {
            // 忽略单个字符加载失败
        }
    }

    /**
     * 特殊字符到文件名的映射
     */
    private String getFileName(String ch)
    {
        switch (ch)
        {
            case "+": return "plus";
            case "-": return "minus";
            case "x": case "X": return "multiply";
            case "÷": case "/": return "divide";
            case "=": return "equals";
            case "?": return "question";
            default: return ch;
        }
    }

    /**
     * 生成验证码文本（字符）
     */
    public String createText()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < charLength; i++)
        {
            int idx = random.nextInt(charList.size());
            sb.append(charList.get(idx));
        }
        return sb.toString();
    }

    /**
     * 根据文本生成验证码图片
     * <p>
     * 若实际字符总宽度超过配置的 width，会自动扩展画布宽度，
     * 避免因 x 起始坐标为负导致字符被画在画布外而出现"显示不全"。
     * </p>
     */
    public BufferedImage createImage(String text)
    {
        // 先计算每个字符的尺寸和整体布局
        int charCount = text.length();
        int[] widths = new int[charCount];
        int[] heights = new int[charCount];
        int maxCharHeight = 0;
        int totalWidth = 0;
        for (int i = 0; i < charCount; i++)
        {
            char c = text.charAt(i);
            int idx = charList.indexOf(String.valueOf(c));
            if (idx >= 0 && idx < charImages.size())
            {
                widths[i] = charImages.get(idx).getWidth();
                heights[i] = charImages.get(idx).getHeight();
            }
            else
            {
                widths[i] = 30;
                heights[i] = 50;
            }
            totalWidth += widths[i];
            if (heights[i] > maxCharHeight)
            {
                maxCharHeight = heights[i];
            }
        }
        totalWidth += charSpacing * (charCount - 1);

        // 自动扩展画布：实际画布宽度为配置宽度与所需最小宽度的较大值
        // 左右各留 6px 内边距，避免旋转后字符贴边被裁剪
        int padding = 6;
        int actualWidth = Math.max(width, totalWidth + padding * 2);
        int actualHeight = Math.max(height, maxCharHeight + padding * 2);

        BufferedImage image = new BufferedImage(actualWidth, actualHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 白色背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, actualWidth, actualHeight);

        // 画字符（水平居中，垂直按最大字符高度居中）
        int x = (actualWidth - totalWidth) / 2;
        int y = (actualHeight - maxCharHeight) / 2;

        for (int i = 0; i < charCount; i++)
        {
            char c = text.charAt(i);
            int idx = charList.indexOf(String.valueOf(c));
            if (idx >= 0 && idx < charImages.size())
            {
                BufferedImage charImg = charImages.get(idx);
                int charCenterX = x + widths[i] / 2;
                int charCenterY = y + heights[i] / 2;
                // 随机旋转 -15° ~ 15°
                int rotation = random.nextInt(30) - 15;
                double theta = Math.toRadians(rotation);
                g.rotate(theta, charCenterX, charCenterY);
                g.drawImage(charImg, x, y, null);
                g.rotate(-theta, charCenterX, charCenterY);
                x += widths[i] + charSpacing;
            }
        }

        // 画干扰线（使用实际画布尺寸）
        g.setColor(new Color(150, 150, 150));
        for (int i = 0; i < 3; i++)
        {
            int x1 = random.nextInt(actualWidth);
            int y1 = random.nextInt(actualHeight);
            int x2 = random.nextInt(actualWidth);
            int y2 = random.nextInt(actualHeight);
            g.drawLine(x1, y1, x2, y2);
        }

        // 画干扰点
        g.setColor(new Color(180, 180, 180));
        for (int i = 0; i < 30; i++)
        {
            int px = random.nextInt(actualWidth);
            int py = random.nextInt(actualHeight);
            g.fillOval(px, py, 2, 2);
        }

        g.dispose();
        return image;
    }

    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public void setCharLength(int charLength) { this.charLength = charLength; }
    public void setCharSpacing(int charSpacing) { this.charSpacing = charSpacing; }
}
