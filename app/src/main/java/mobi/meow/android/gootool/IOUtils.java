/*
 * Copyright (c) 2013-2018 MeowCat Studio Powered by MlgmXyysd All Rights Reserved.
 */

package mobi.meow.android.gootool;

import static mobi.meow.android.gootool.MeowCatApplication.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import kellinwood.zipio.ZioEntry;
import kellinwood.zipio.ZipInput;
import kellinwood.zipio.ZipOutput;

public class IOUtils {
    private static final Field ZIO_ENTRY_DATA;

    static {
        try {
            ZIO_ENTRY_DATA = ZioEntry.class.getDeclaredField("data");
            ZIO_ENTRY_DATA.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final boolean DEBUG = true; // Set to true to enable logging

    private static long getUncompressedZipSize(File f) throws IOException {
        //get the zip file content
        ZipInputStream zis =
                new ZipInputStream(new FileInputStream(f));
        ZipEntry e;
        long s = 0;
        while ((e = zis.getNextEntry()) != null) {
            s += e.getSize();
        }
        return s;
    }


    public static InputStream getResource(String path) {
        try {
            return WoGInitData.getContext().getAssets().open(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void extractZip(File f, File to, ProgressListener pl) {
        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            if (!to.exists()) {
                to.mkdir();
            }

            final long size = getUncompressedZipSize(f);
            long unpackedBytes = 0;
            pl.progressStep(0.08f);
            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(f));
            //get the zipped file list entry
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {

                String fileName = ze.getName();
                File newFile = new File(to + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(Objects.requireNonNull(newFile.getParent())).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    unpackedBytes += len;
                    pl.progressStep((unpackedBytes / (float) size) * 0.9F + 0.1F);
                }

                fos.close();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static Set<File> getAllFilesToAdd(File sourceDir) throws IOException {
        Set<File> set = new HashSet<>();
        scanDirectoryRecursive(sourceDir, set);
        return set;
    }

    /**
     * Uses given ZipBase as source zip file for zipio library and replaces content of existing files with files in soedified location.
     */
    public static void zipDirContentWithZipBase(File baseZip, File sourceDir, File outFile) throws IOException {
        //Hacked to support writing World of Goo APK...
        ZipInput zin = ZipInput.read(baseZip.getPath());
        ZipOutput zout = new ZipOutput(outFile);

        Set<File> filesToAdd = getAllFilesToAdd(sourceDir);

        Map<String, ZioEntry> zinEntries = zin.getEntries();

        //new entries will be assets, so take example asset
        //zipio will copy properties of example file like zip specification version, compression method etc...
        //they can't be set in any other way, except reflection hacks
        //and because we use hacks to decrease memory usage - we need to get cloned entry before we add files to apk.
        ZioEntry baseEntry = Objects.requireNonNull(zinEntries.get("assets/res/islands/island1.xml.mp3")).getClonedEntry("UNNAMED");

        Iterator<Map.Entry<String, ZioEntry>> it = zinEntries.entrySet().iterator();
        //write entries that are there already
        while (it.hasNext()) {
            Map.Entry<String, ZioEntry> entry = it.next();
            File f = new File(sourceDir, entry.getKey()).getCanonicalFile();
            if (!f.exists()) {
                Log.w(TAG, "File doesn't exist, skipping: " + f);
//                Assert.that(!filesToAdd.contains(f));
                continue;
            }
            Log.i(TAG, "Zipping file: " + f);
            InputStream in = new BufferedInputStream(new FileInputStream(f));
            OutputStream os = entry.getValue().getOutputStream();
            writeInToOut(in, os);
            os.close();
            zout.write(entry.getValue());
            in.close();
            filesToAdd.remove(f);
            it.remove();
            //HACK HACK HACK!
            //Force zipio to delete current OutputStream by creating a new one
            //It will still leave the ZioEntry in memory, but the big ByteArrayOutputStream will be deleted
            entry.getValue().getOutputStream();
            //HACK2!
            //Use reflection to clear data field in ZioEntry
            //At this point data is already written so this field won't be used anymore
            try {
                ZIO_ENTRY_DATA.set(entry.getValue(), null);
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }


        int stripLength = sourceDir.getCanonicalPath().length() + 1;
        //add all other files
        for (File f : filesToAdd) {
            ZioEntry e = baseEntry.getClonedEntry(f.getPath().substring(stripLength));
            InputStream in = new BufferedInputStream(new FileInputStream(f));
            OutputStream os = e.getOutputStream();
            writeInToOut(in, os);
            os.close();
            zout.write(e);
            in.close();
            //Hacks simillar to these above. Just in case the ZioEntry is stored SOMEWHERE:
            e.getOutputStream();
            try {
                ZIO_ENTRY_DATA.set(e, null);
            } catch (IllegalAccessException ex) {
                throw new AssertionError(ex);
            }
        }

        zout.close();
        zin.close();
    }

    public static void writeInToOut(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[16 * 1024];
        int l;
        while ((l = in.read(buf)) != -1) {
            out.write(buf, 0, l);
        }
    }

    private static void scanDirectoryRecursive(File dir, Set<File> set) throws IOException {
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) {
                scanDirectoryRecursive(f, set);
                continue;
            }
            set.add(f.getCanonicalFile());
        }
    }

    public static void deleteDirContent(File dir) {
        for (File sub : Objects.requireNonNull(dir.listFiles())) {
            deleteFile(sub);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            deleteDirContent(file);
        }
        file.delete();
    }

    public static void copyFilesExcept(File src, File dest, String... ignoredFiles) {
        Set<String> toIgnore = new HashSet<>(Arrays.asList(ignoredFiles));

        try {
            copyFilesExcept(src, dest, toIgnore);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void copyFilesExcept(File src, File dest, Set<String> toIgnore) throws IOException {
        for (File file : Objects.requireNonNull(src.listFiles())) {
            if (toIgnore.contains(file.getName())) {
                continue;
            }
            if (file.isDirectory()) {
                File dir = new File(dest, file.getName());
                dir.mkdir();
                copyFilesExcept(file, dir, toIgnore);
            } else {
                Utilities.copyFile(file, new File(dest, file.getName()));
            }
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    public static boolean isLocal(String url) {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        Log.d(TAG, uri.toString());

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            DatabaseUtils.dumpCursor(cursor);
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br>
     * <br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see #isLocal(String)
     * @see #getFile(Context, Uri)
     */
    @SuppressLint("NewApi")
    public static String getPath(final Context context, Uri uri) {

        if (DEBUG)
            Log.d(TAG + " File -",
                    "Authority: " + uri.getAuthority() +
                            ", Fragment: " + uri.getFragment() +
                            ", Port: " + uri.getPort() +
                            ", Query: " + uri.getQuery() +
                            ", Scheme: " + uri.getScheme() +
                            ", Host: " + uri.getHost() +
                            ", Segments: " + uri.getPathSegments().toString()
            );

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                return getDataColumn(context, uri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                assert contentUri != null;
                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else {
                //it may be LocalStorageProvider, I don't know how to check for it
                // The path is the id
                return DocumentsContract.getDocumentId(uri);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see #getPath(Context, Uri)
     */
    public static File getFile(Context context, Uri uri) {
        if (uri != null) {
            String path = getPath(context, uri);
            if (isLocal(path)) {
                return new File(path);
            }
        }
        return null;
    }

}
