package ImgTool;

import javafx.scene.paint.Color;

/**
 * @author huyue
 * @date 2018/11/16-23:25
 */

/**图像工具1.0 支持：
 * 动态模糊
 * 灰度转换
 * 边缘检测（sobel算法）
 */
public class ImgTool {
    //转灰度算法
    public static Color[][] toGray(Color[][] src){
        Color[][] fin=new Color[src.length][src[0].length];
        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                double temp=src[i][j].getRed()*0.3+src[i][j].getBlue()*0.11+src[i][j].getGreen()*0.59;
                fin[i][j]=Color.color(temp,temp,temp);
            }
        }
        return fin;
    }


    //边缘检测
    public static final int[][] sobelX={
            {-1, 0,+1},
            {-2, 0,+2},
            {-1, 0,+1},
    };
    public static final int[][] sobelY={
            {-1,-2,-1},
            { 0, 0, 0},
            {+1,+2,+1},
    };
    private static double getX(Color[][] src,int x,int y){
        double fin=0;

        for (int i=0;i<sobelX.length;i++){
            for (int j=0;j<sobelX[0].length;j++){
                fin+=src[y-1+i][x-1+j].getBlue()*sobelX[i][j];
            }
        }

        return fin;
    }
    private static double getY(Color[][] src,int x,int y){
        double fin=0;

        for (int i=0;i<sobelY.length;i++){
            for (int j=0;j<sobelY[0].length;j++){
                fin+=src[y-1+i][x-1+j].getBlue()*sobelY[i][j];
            }
        }

        return fin;
    }
    public static Color[][] edgeCoarse(Color[][] src){
        Color[][] fin=new Color[src.length][src[0].length];

        for (int i=0;i<src.length;i++){
            for (int j=0;j<src[0].length;j++){
                if (i==0 || i==src.length-1 || j==0 || j==src[0].length-1){
                    fin[i][j]=Color.color(0,0,0);
                }else {
                    double t=Math.sqrt(getX(src,j,i)*getX(src,j,i)+getY(src,j,i)*getY(src,j,i));
                    if (t>1.0) t=1.0;
                    fin[i][j]=Color.color(t,t,t);
                }
            }
        }

        return fin;
    }


    //动态模糊功能
    //动态模糊函数
    public static Color[][] motionBlur(Color[][] src,double roate){
        Color[][] fin=new Color[src.length][src[0].length];

        //核的大小
        int size;
        if (src.length<src[0].length){
            size=src.length/60*2+1;
        }else {
            size=src[0].length/60*2+1;
        }

        //创建核
        double[][] k=genConvolutionKernel(size,roate);

        //动态模糊
        for (int i=0;i<fin.length;i++){
            for (int j=0;j<fin[0].length;j++){
                double t=getHendal(src,k,j,i);
                fin[i][j]=Color.color(t,t,t);
            }
        }


        return fin;
    }
    //生成卷积核函数
    private static double[][] genConvolutionKernel(int width,double roate){
        double[][] kernel=new double[width][width];

        if (roate==270){
            for (int i=0;i<kernel.length/2;i++){
                kernel[i][width/2]=1;
            }
            return kernel;
        }
        if (roate==90){
            for (int i=kernel.length/2;i<kernel.length;i++){
                kernel[i][width/2]=1;
            }
            return kernel;
        }
        if ((roate>90 && roate<270)){
            double tan=Math.tan(Math.PI*roate/180);
            for (double i=0;i<(double)width/2;i+=0.1){
                if ((int)((double)width/2-(tan*i))>=0 && (int)((double)width/2-(tan*i))<kernel.length)
                    kernel[(int)((double)width/2-(tan*i))][(int)(i+(double)width/2)]=1;
            }
            return kernel;
        }
        if ((roate>=0 && roate<90)||(roate>=270 && roate<360)){
            double tan=Math.tan(Math.PI*roate/180);
            for (double i=-(double)width/2+0.1;i<=0;i+=0.1){
                if ((int)((double)width/2-(tan*i))>=0 && (int)((double)width/2-(tan*i))<kernel.length)
                    kernel[(int)((double)width/2-(tan*i))][(int)(i+(double)width/2)]=1;
            }
        }

        return kernel;
    }
    //动态模糊卷积核调用
    private static double getHendal(Color[][] src,double[][] k,int x,int y){
        double fin=0;
        double sum=0;

        for (int i=0;i<k.length;i++){
            for (int j=0;j<k[0].length;j++){
                int y1=y-(k.length-1)/2+i;
                int x1=x-(k.length-1)/2+j;
                if (y1>=0 && y1<src.length && x1>=0 && x1<src[0].length) {
                    fin += src[y1][x1].getBlue() * k[i][j];
                    sum += k[i][j];
                }
            }
        }

        return fin/sum;
    }
}
