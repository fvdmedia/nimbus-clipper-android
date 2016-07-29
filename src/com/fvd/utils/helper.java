package com.fvd.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

public class helper {

	public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
          return data;
        } finally {
            f.close();
        }
    }
    
    public static String ExtractBtw(String text, String sFrom, String sTo){
		String result = "";
        int ifrom = text.indexOf(sFrom);
        if (ifrom != -1)
        {
            int ito = text.indexOf(sTo, ifrom + sFrom.length());
            if (ito != -1)
            {
                result = text.substring(ifrom + sFrom.length(), ito);
            }
        }
        if (result==null) result="";
        return result;
	}

    
    public static String extractFileName(String fullFileName){
        return fullFileName.substring(fullFileName.lastIndexOf("/")+1, fullFileName.length() );
    }
    
    public static String getRandomString(int len)
    {
        String str = (Long.toHexString(Double.doubleToLongBits(Math.random()))).substring(0, len);
        return str;
    } 
    
    public static int getOrientationFromExif(String imagePath) {
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            
        }

        return orientation;
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        	return inSampleSize;
        }
        
        
        public static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path,options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(path,options);
        }
        
        public static Bitmap LoadImageFromWeb(String url){
            try{
            	String encodedurl = url.replace(" ", "%20");
            	InputStream is = (InputStream) new URL(encodedurl).getContent();
            	Bitmap d = BitmapFactory.decodeStream(is);
            	return d;
            }
            catch (Exception e) {
            	e.printStackTrace();
            	return null;
            }
        }
}
