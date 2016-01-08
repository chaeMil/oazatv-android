package com.chaemil.hgms.utils;

import java.text.DecimalFormat;

/**
 * Created by chaemil on 8.1.16.
 */
public class StringUtils {
    public static String getStringSizeLengthFile(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMo = sizeKb * sizeKb;
        float sizeGo = sizeMo * sizeKb;
        float sizeTerra = sizeGo * sizeKb;


        if(size < sizeMo)
            return df.format(size / sizeKb)+ " Kb";
        else if(size < sizeGo)
            return df.format(size / sizeMo) + " Mb";
        else if(size < sizeTerra)
            return df.format(size / sizeGo) + " Gb";

        return "";
    }
}
