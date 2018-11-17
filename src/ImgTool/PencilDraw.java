package ImgTool;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author huyue
 * @date 2018/11/17-19:45
 */
public class PencilDraw {
    public static Image toPencilDraw(Image image){
        PixelReader reader=image.getPixelReader();
        javafx.scene.paint.Color[][] src=new javafx.scene.paint.Color[(int)image.getHeight()][(int)image.getWidth()];
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                src[i][j]=reader.getColor(j,i);
            }
        }

        //灰度化处理
        src = ImgTool.toGray(src);

        //边缘检测处理
        src = ImgTool.edgeCoarse(src);
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                if (src[i][j].getGreen()<0.05){
                    src[i][j]= javafx.scene.paint.Color.color(0,0,0);
                }
            }
        }

        //动态模糊：八个方向
        javafx.scene.paint.Color[][][] motionBlurs=new javafx.scene.paint.Color[8][][];
        for (int i=0;i<motionBlurs.length;i++){
            motionBlurs[i]=ImgTool.motionBlur(src,180.0*i/8);
        }
//        src = ImgTool.ImgTool.motionBlur(src,120);

        //取笔画处理
        javafx.scene.paint.Color[][][] paintss=new javafx.scene.paint.Color[8][src.length][src[0].length];
        for (int i=0;i<paintss.length;i++){
            for (int j=0;j<paintss[0].length;j++){
                Arrays.fill(paintss[i][j], javafx.scene.paint.Color.color(0,0,0));
            }
        }
        double max;
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                max=motionBlurs[0][i][j].getBlue();
                int point=0;
                for (int index=1;index<paintss.length;index++){
                    if (max<motionBlurs[index][i][j].getBlue()){
                        max=motionBlurs[index][i][j].getBlue();
                        point=index;
                    }
                }
                paintss[point][i][j]=motionBlurs[point][i][j];
            }
        }

        //笔画相加
        javafx.scene.paint.Color[][] addFin=new javafx.scene.paint.Color[src.length][src[0].length];
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                for (int point=0;point<paintss.length;point++){
                    if (paintss[point][i][j].getGreen()>0){
                        addFin[i][j]=paintss[point][i][j];
                    }
                }
            }
        }
        src=addFin;
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                if (src[i][j]==null){
                    src[i][j]= javafx.scene.paint.Color.color(0,0,0);
                }
            }
        }

        //相加结果取反
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                double t=1.0-src[i][j].getBlue();
                src[i][j]= javafx.scene.paint.Color.color(t,t,t);
            }
        }

        //滤镜
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                double t=src[i][j].getBlue();
                if (t>0.25){
                    src[i][j]= Color.color(1,1,1);
                }
            }
        }

        WritableImage img=new WritableImage(src[0].length,src.length);
        PixelWriter set=img.getPixelWriter();
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                set.setColor(j,i,src[i][j]);
            }
        }

        return img;
    };
    public static void writeFile(Image img, File outputFile){
        BufferedImage bImage = SwingFXUtils.fromFXImage(img,null);
        try {
            ImageIO.write(bImage, "png", outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
