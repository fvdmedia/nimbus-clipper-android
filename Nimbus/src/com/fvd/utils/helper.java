package com.fvd.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import com.fvd.cropper.R.string;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

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
    
    public static String trimExt(String fn){
    	int i=fn.lastIndexOf(".");
    	if(i!=-1) return fn.substring(0,i);
    	return fn;
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
    
    
    public static int getImageRotation(Context context, Uri imageUri) {
        try {
            ExifInterface exif = new ExifInterface(imageUri.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            if (rotation == ExifInterface.ORIENTATION_UNDEFINED)
                return getRotationFromMediaStore(context, imageUri);
            else return exifToDegrees(rotation);
        } catch (IOException e) {
            return 0;
        }
    }

    public static String saveTmp(InputStream is) {
    	String savingPath=appSettings.getSavingPath();
		String	photoFileName=String.valueOf(System.currentTimeMillis())+"-tmp.jpg";
		File file = new File(savingPath,photoFileName); 
		try{		
			OutputStream os=new FileOutputStream(file);
			byte[] buffer = new byte[1024*500];
			int len;
			while ((len = is.read(buffer)) != -1) {
			    os.write(buffer, 0, len);
			}
			is.close();
			//os.flush();
			os.close();
		}
		catch (Exception e){}
    	return file.getPath();
	}
    
    public static int getRotationFromMediaStore(Context context, Uri imageUri) {
        String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(imageUri, columns, null, null, null);
        if (cursor == null) return 0;

        cursor.moveToFirst();

        int orientationColumnIndex = cursor.getColumnIndex(columns[1]);
        return cursor.getInt(orientationColumnIndex);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } else if (exifOrientation == ExifInterface.ORIENTATION_NORMAL) {
            return 0;
        } else {
            return 0;
        }
    }
    
    public static int getOrientationFromExif(String imagePath) {
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
                    ExifInterface.ORIENTATION_NORMAL);
            //if(exifOrientation==ExifInterface.ORIENTATION_UNDEFINED) orientation=-1;
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
        
        
        public static Bitmap decodeSampledBitmap(String path, boolean isPhoto) {

            int reqW=1000; 
            Bitmap b;
	        	final BitmapFactory.Options options = new BitmapFactory.Options();
	            options.inJustDecodeBounds = true;
	            BitmapFactory.decodeFile(path,options);
	            options.inSampleSize = calculateInSampleSize(options, reqW, reqW);
	            options.inJustDecodeBounds = false;
	            b= BitmapFactory.decodeFile(path,options);
	            if(isPhoto && appSettings.photo_quality<95){
	            	try{
		            	ByteArrayOutputStream bos = new ByteArrayOutputStream();
						b.compress(CompressFormat.JPEG, appSettings.photo_quality, bos);
						byte[] data=bos.toByteArray();
						b=BitmapFactory.decodeByteArray(data, 0, data.length);
	            	} catch (Exception e){}
	            }
	            return b;
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
