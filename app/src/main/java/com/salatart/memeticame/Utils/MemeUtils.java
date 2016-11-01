package com.salatart.memeticame.Utils;

import com.salatart.memeticame.Models.Meme;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sasalatart on 10/31/16.
 */

public class MemeUtils {
    public static String createName(String baseName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return baseName.replace(' ', '_') + Meme.SEPARATOR + timestamp + ".jpg";
    }

    public static String cleanName(String fullName) {
        int indexOfSeparator = fullName.indexOf(Meme.SEPARATOR);
        return fullName.substring(0, indexOfSeparator);
    }
}
