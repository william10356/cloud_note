package com.example.notes.Util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class MyBitmapUtil {
    public  static Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成bitmap
    {
        int width = drawable.getIntrinsicWidth();// 取drawable的长宽
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ?Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;// 取drawable的颜色格式
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);// 建立对应bitmap
        Canvas canvas = new Canvas(bitmap);// 建立对应bitmap的画布
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);// 把drawable内容画到画布中
        return bitmap;
    }

    //两个多态方法获取bitmap
    public static String bitmapToString(String filepath){
        Bitmap bitmap = getSmallBitmap(filepath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 1.5M的压缩后在100Kb以内，测试得值,压缩后的大小=94486字节,压缩后的大小=74473字节
        // 这里的JPEG 如果换成PNG，那么压缩的就有600kB这样.
        // 实际项目中，可以根据需要考虑图片压缩以及压缩的质量。
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, baos);
        byte[] b = baos.toByteArray();
        // 在这里获取到图片转换后的字符串，然后就可以将这个字符串当做普通的String字符串参数传给后台
        // 如果有很多张图片要上传，那么可以考虑将转换后的Base64字符串添加到一个List里面，一并传给后台。
        return Base64.encodeToString(b,Base64.DEFAULT).replaceAll("[\\s*\t\n\r]", "");
    }
    public static String bitmapToString(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.PNG, 40, bos);//参数100表示不压缩
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT).replaceAll("[\\s*\t\n\r]", "");
    }
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }



    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }
    //计算图片的缩放质量
    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
