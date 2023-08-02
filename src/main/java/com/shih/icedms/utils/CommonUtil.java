package com.shih.icedms.utils;

import org.springframework.util.StringUtils;

public class CommonUtil {
    public static String getFileNameWithOutExt(String fileName){
        if(StringUtils.hasText(getFilenameExtensionWithoutDot(fileName))){
            return fileName.substring(0,fileName.lastIndexOf(getFilenameExtensionWithDot(fileName)));
        }else{
            return fileName;
        }
    }
    public static String getFilenameExtensionWithDot(String fileName){
        return (StringUtils.hasText(StringUtils.getFilenameExtension(fileName))?"."+StringUtils.getFilenameExtension(fileName):"");
    }
    public static String getFilenameExtensionWithoutDot(String fileName){
        return (StringUtils.hasText(StringUtils.getFilenameExtension(fileName))?StringUtils.getFilenameExtension(fileName):"");
    }
}
