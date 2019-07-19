package httpserver.util;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicMatch;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;



public class FileUtil {
    public static String getMimeType(File file) {
        String mimeType;
        try {
            MagicMatch match = Magic.getMagicMatch(file, false);
            mimeType = match.getMimeType();
            if (StringUtils.isEmpty(mimeType)) {
                if (file.getName().endsWith(".js"))
                    mimeType = "application/javascript";
                else
                    mimeType = "application/octet-stream";
            }
        } catch (Throwable ex) {
            if (file.getName().endsWith(".js"))
                mimeType = "application/javascript";
            else
                mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    public static boolean isImageExt(String ext) {
        if (StringUtils.isBlank(ext)) return false;
        ext = StringUtils.lowerCase(ext, Locale.ENGLISH);
        String s = "|gif|jpg|jpeg|png|bmp|";
        return StringUtils.contains(s, "|" + ext + "|");
    }

    public static String getMimeType(byte[] file) {
        String mimeType;
        try {
            MagicMatch match = Magic.getMagicMatch(file, false);
            mimeType = match.getMimeType();
            if (StringUtils.isEmpty(mimeType)) {
                mimeType = "application/octet-stream";
            }
        } catch (Throwable ex) {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) return true;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    boolean success = deleteDir(file);
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir.delete();
    }
}
