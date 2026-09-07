import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**
 * 生成验证码额外字符图片（运算符等）
 */
public class GenerateExtraChars {
    public static void main(String[] args) throws Exception {
        int w = 40, h = 50;
        Font font = new Font("Arial", Font.BOLD, 38);
        File outDir = new File(args.length > 0 ? args[0] : "/tmp/captcha-chars");
        outDir.mkdirs();

        // 额外字符映射
        String[][] mappings = {
            {"+", "plus"},
            {"-", "minus"},
            {"x", "multiply"},
            {"÷", "divide"},
            {"=", "equals"},
            {"?", "question"}
        };

        for (String[] mapping : mappings) {
            String ch = mapping[0];
            String fileName = mapping[1];
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(font);
            g.setColor(Color.BLACK);
            FontMetrics fm = g.getFontMetrics();
            int x = (w - fm.stringWidth(ch)) / 2;
            int y = fm.getAscent();
            g.drawString(ch, x, y);
            g.dispose();
            ImageIO.write(img, "png", new File(outDir, fileName + ".png"));
            System.out.println("Generated: " + fileName + ".png for '" + ch + "'");
        }
        System.out.println("Done!");
    }
}
