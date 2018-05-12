package bot;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageSwizzler {

    final static String[] swizzles = {
            "rbg",
            "grb",
            "brg",
            "gbr",
            "bgr",
            "gbb"
    };



    public InputStream swizzle(InputStream imgStream, @Nullable String arg) throws IOException {
        BufferedImage img = ImageIO.read(imgStream);
        int[][][] channels = splitByChannels(img);

        BufferedImage output;
        if(arg == null)
            output = createAllSwizzles(img, channels);
        else
            output = createSwizzledImage(img.getWidth(), img.getHeight(), channels, swizzles[Integer.valueOf(arg) - 1]);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(output, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }



    private BufferedImage createSwizzledImage(int width, int height, int[][][] channels, String swizzle) {

        int channelOne = getChannelIndexByChar(swizzle.charAt(0));
        int channelTwo = getChannelIndexByChar(swizzle.charAt(1));
        int channelThree = getChannelIndexByChar(swizzle.charAt(2));

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for(int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int a = channels[0][col][row];
                int r = channels[channelOne][col][row];
                int g = channels[channelTwo][col][row];
                int b = channels[channelThree][col][row];
                int pixel = (a << 24) | (r << 16) | (g << 8) | b;
                img.setRGB(col, row, pixel);
            }
        }
        return img;
    }



    private int getChannelIndexByChar(char c) {
        if(c == 'r') return 1;
        else if(c == 'g') return 2;
        else if(c == 'b') return 3;
        else return 0;
    }



    private int[][][] splitByChannels(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        /* An array that contains 4 channels, with each channel being represented by matrix of integers.
         the channel order is ARGB. */
        int[][][] channels = new int[4][width][height];
        for(int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int rgb = img.getRGB(col, row);
                channels[0][col][row] = (rgb >> 24) & 0x000000FF;
                channels[1][col][row] = (rgb >> 16) & 0x000000FF;
                channels[2][col][row] = (rgb >> 8) & 0x000000FF;
                channels[3][col][row] = (rgb) & 0x000000FF;
            }
        }
        return  channels;
    }



    private BufferedImage createAllSwizzles(BufferedImage img, int[][][] channels) {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage[] images = new BufferedImage[6];
        for (int i = 0; i < 6; i++)
            images[i] = createSwizzledImage(width, height, channels, swizzles[i]);

        BufferedImage output = new BufferedImage(width * 3, height * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(images[0], null, 0, 0);
        g2d.drawImage(images[1], null, width, 0);
        g2d.drawImage(images[2], null, width * 2, 0);
        g2d.drawImage(images[3], null, 0, height);
        g2d.drawImage(images[4], null, width, height);
        g2d.drawImage(images[5], null, width * 2, height);
        g2d.dispose();

        return output;
    }
}
