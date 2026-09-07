import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**
 * 生成验证码字符图片（0-9, A-Z）
 * 在 macOS 上运行，生成的图片打包进 jar，供 Linux 服务器使用
 */
public class GenerateCaptchaFont {
    public static void main(String[] args) throws Exception {
        String chars = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // 去掉易混淆字符 I/O
        int w = 40, h = 50;
        Font font = new Font("Arial", Font.BOLD, 38);

        File outDir = new File(args.length > 0 ? args[0] : "/tmp/captcha-chars");
        outDir.mkdirs();

        for (int i = 0; i < chars.length(); i++) {
            String ch = String.valueOf(chars.charAt(i));
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

            ImageIO.write(img, "png", new File(outDir, ch + ".png"));
        }
        System.out.println("Generated " + chars.length() + " character images in " + outDir.getAbsolutePath());
    }
}
