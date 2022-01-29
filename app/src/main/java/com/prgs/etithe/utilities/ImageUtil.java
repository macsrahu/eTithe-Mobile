package com.prgs.etithe.utilities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtil {

    static String mOutputFilePath = "";

    public static String PrepareImage(Context mContext, int requestCode, int resultCode, Intent data, int RESULT_OK, ImageView imgPicture, String _FOLDER_PATH, String FILE_NAME) throws IOException {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                //startCropImageActivity(imageUri);
                File externalCacheFile = new File(mContext.getExternalCacheDir().toString());
                for (File temp : externalCacheFile.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        externalCacheFile = temp;
                        break;
                    }
                }
                try {

                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                    bitmap = BitmapFactory.decodeFile(externalCacheFile.getAbsolutePath(),
                            bitmapOptions);
                    if (bitmap != null) {

                        bitmap = Global.rotateImageIfRequired(mContext, bitmap, Uri.fromFile(externalCacheFile));
                        imgPicture.setImageBitmap(bitmap);

                        File exportDir = new File(mContext.getExternalCacheDir(), _FOLDER_PATH);
                        if (!exportDir.exists()) {
                            exportDir.mkdirs();
                        }
                        externalCacheFile.delete();

                        OutputStream outFile = null;
                        File file = new File(exportDir, FILE_NAME + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                        try {
                            outFile = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                            mOutputFilePath = file.getAbsolutePath();


                            outFile.flush();
                            outFile.close();

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Messages.ShowToast(mContext, "Unable to set image option 1");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
//                String[] filePath = {MediaStore.Images.Media.DATA};
//                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
//                c.moveToFirst();
//                int columnIndex = c.getColumnIndex(filePath[0]);
//                String picturePath = c.getString(columnIndex);
//                c.close();
//
                String picturePath = RealPathUtil.getRealPath(mContext, selectedImage);
                //File fileAbs = new File(selectedImage.getPath());
                //picturePath = fileAbs.getAbsolutePath();
                //Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                //imgPicture.setImageBitmap(thumbnail);
                if (!picturePath.isEmpty()) {

                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(picturePath,
                            bitmapOptions);

                    if (bitmap != null) {
                        bitmap = Global.rotateImageIfRequired(mContext, bitmap, selectedImage);
                        imgPicture.setImageBitmap(bitmap);

                        File exportDir = new File(mContext.getExternalCacheDir(), _FOLDER_PATH);
                        if (!exportDir.exists()) {
                            exportDir.mkdirs();
                        }

                        OutputStream outFile = null;
                        File file = new File(exportDir, FILE_NAME + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                        try {
                            outFile = new FileOutputStream(file);
                            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile)) {
                                mOutputFilePath = file.getAbsolutePath();
                                outFile.flush();
                                outFile.close();
                            } else {
                                Messages.ShowToast(mContext, "Unable to compress");
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        }
                    }
                }


            } else if (requestCode == 3) {

                Uri selectedImageUri = data.getData();
                String selectedImagePath;
                //ADDED
                String filemanagerstring;
                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();
                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri, mContext);
                ///Toast.makeText(mContext, selectedImagePath, LENGTH_LONG).show();


                //DEBUG PURPOSE - you can delete this if you want
                if (selectedImagePath != null)
                    System.out.println(selectedImagePath);
                else System.out.println("selectedImagePath is null");
                if (filemanagerstring != null)
                    System.out.println(filemanagerstring);
                else System.out.println("filemanagerstring is null");


                //NOW WE HAVE OUR WANTED STRING
                if (selectedImagePath != null)
                    System.out.println("selectedImagePath is the right one for you!");
                else
                    System.out.println("filemanagerstring is the right one for you!");
                try {


                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(selectedImagePath,
                            bitmapOptions);
                    if (bitmap != null) {
                        bitmap = Global.rotateImageIfRequired(mContext, bitmap, selectedImageUri);
                        imgPicture.setImageBitmap(bitmap);


                        File exportDir = new File(mContext.getCacheDir(), _FOLDER_PATH);
                        if (!exportDir.exists()) {
                            exportDir.mkdirs();
                        }

                        OutputStream outFile = null;
                        File file = new File(exportDir, FILE_NAME + "_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                        try {
                            outFile = new FileOutputStream(file);
                            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile)) {
                                mOutputFilePath = file.getAbsolutePath();
                                //Toast.makeText(mContext, mOutputFilePath, Toast.LENGTH_LONG).show();
                                outFile.flush();
                                outFile.close();
                            } else {
                                Messages.ShowToast(mContext, "Unable to compress");
                            }

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Messages.ShowToast(mContext, e.getMessage());
                        }
                    }

                } catch (Exception ex) {
                    Messages.ShowToast(mContext, ex.getMessage());
                }

            }
        }
        return mOutputFilePath;
    }

    private static String getPath(Uri contentUri, Context mContext) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = null;
        try {
            if (Build.VERSION.SDK_INT > 19) {
                // Will return "image:x*"
                String wholeID = DocumentsContract.getDocumentId(contentUri);
                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];
                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";

                cursor = mContext.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        projection, sel, new String[]{id}, null);
            } else {
                cursor = mContext.getContentResolver().query(contentUri,
                        projection, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String path = null;
        try {
            int column_index = cursor
                    .getColumnIndex(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index).toString();
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return path;
    }


}