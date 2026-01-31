package com.rhineai.framework.util


import cn.hutool.core.io.FileUtil
import java.text.SimpleDateFormat
import java.util.*

class FileUtils {
    private val hexStr = "0123456789ABCDEF"

    /**
     * 通过逆向雪花算法时间戳 年/月/日/时/分打为16进制地址，然后workId和序列号16打为16进制文件名，最终配合ext获取文件完整地址
     */
    fun getFilePath(fileId: String, ext: String): String {
        // 转为二进制
        val byteCode = java.lang.Long.toBinaryString(fileId.toLong())
        //获取时间位
        val time = byteCode.substring(0, 41)
        // 获取时间戳
        val timestamp = time.toLong(2)
        val date = Date(timestamp)
        val simpleDateFormatYMD = SimpleDateFormat("yyyyMMdd")
        val ymd = simpleDateFormatYMD.format(date)
        val simpleDateFormatHM = SimpleDateFormat("HHmmssSSSS")
        val hm = simpleDateFormatHM.format(date).toLong()
        val yhdhm = ymd + hm
        return "/" + ymd + "/" + java.lang.Long.toHexString(
            hm
        ) + "/" + java.lang.Long.toHexString(fileId.toLong()) + "." + ext
    }

    fun getFilePathNoProfile(fileId: String, ext: String): String {
        // 转为二进制
        val byteCode = java.lang.Long.toBinaryString(fileId.toLong())
        //获取时间位
        val time = byteCode.substring(0, 41)
        // 获取时间戳
        val timestamp = time.toLong(2)
        val date = Date(timestamp)
        val simpleDateFormatYMD = SimpleDateFormat("yyyyMMdd")
        val ymd = simpleDateFormatYMD.format(date)
        val simpleDateFormatHM = SimpleDateFormat("HHmmssSSSS")
        val hm = simpleDateFormatHM.format(date).toLong()
        val yhdhm = ymd + hm
        return "/" + ymd + "/" + java.lang.Long.toHexString(hm) + "/" + java.lang.Long.toHexString(
            fileId.toLong()
        ) + "." + ext
    }

    fun getPath(fileId: String): String {
        // 转为二进制
        val byteCode = java.lang.Long.toBinaryString(fileId.toLong())
        //获取时间位
        val time = byteCode.substring(0, 41)
        // 获取时间戳
        val timestamp = time.toLong(2)
        val date = Date(timestamp)
        val simpleDateFormatYMD = SimpleDateFormat("yyyyMMdd")
        val ymd = simpleDateFormatYMD.format(date)
        val simpleDateFormatHMSSss = SimpleDateFormat("HHmmssSSSS")
        val hm = simpleDateFormatHMSSss.format(date).toLong()
        return  "/" + ymd + "/" + java.lang.Long.toHexString(
            hm
        ) + "/"
    }

    fun getPathNoProfile(fileId: String): String {
        // 转为二进制
        val byteCode = java.lang.Long.toBinaryString(fileId.toLong())
        //获取时间位
        val time = byteCode.substring(0, 41)
        // 获取时间戳
        val timestamp = time.toLong(2)
        val date = Date(timestamp)
        val simpleDateFormatYMD = SimpleDateFormat("yyyyMMdd")
        val ymd = simpleDateFormatYMD.format(date)
        val simpleDateFormatHMSSss = SimpleDateFormat("HHmmssSSSS")
        val hm = simpleDateFormatHMSSss.format(date).toLong()
        return  "/" + ymd + "/" + java.lang.Long.toHexString(hm) + "/"
    }


    // public static String getPath(String fileId) {
    //     // 转为二进制
    //     String byteCode = Long.toBinaryString(Long.parseLong(fileId));
    //     //获取时间位
    //     String time = byteCode.substring(0, 41);
    //     // 获取时间戳
    //     Long timestamp = Long.parseLong(time, 2);
    //     Date date = new Date(timestamp);
    //     SimpleDateFormat simpleDateFormatYMD = new SimpleDateFormat("yyyyMMdd");
    //     String ymd = simpleDateFormatYMD.format(date);
    //     SimpleDateFormat simpleDateFormatHMSSss = new SimpleDateFormat("HHmmssSSSS");
    //     Long hm = Long.valueOf(simpleDateFormatHMSSss.format(date));
    //     return CommonConstants.RESOURCE_PREFIX + "/" + ymd + "/" + Long.toHexString(hm) + "/";
    // }
    /**
     * 通过逆向雪花算法时间戳 年/月/日/时/分打为16进制地址，然后workId和序列号16打为16进制文件名，最终配合ext获取文件完整地址
     */
    fun getFileName(fileId: String, ext: String): String {
        // 转为二进制
//        String byteCode = Long.toBinaryString(Long.parseLong(fileId));
//        //获取时间位
//        String time = byteCode.substring(0, 41);
//        // 获取时间戳
//        Long timestamp = Long.parseLong(time, 2);
//        Date date = new Date(timestamp);
//        SimpleDateFormat simpleDateFormatYMD = new SimpleDateFormat("yyyyMMdd");
//        String ymd = simpleDateFormatYMD.format(date);
//        SimpleDateFormat simpleDateFormatHM = new SimpleDateFormat("HHmm");
//        String hm = simpleDateFormatHM.format(date);
//        String yhdhm = ymd + hm;
        return java.lang.Long.toHexString(fileId.toLong()) + "." + ext
    }

    /**
     * 通过逆向雪花算法时间戳 年/月/日/时/分打为16进制地址，然后workId和序列号16打为16进制文件名，最终配合ext获取文件完整地址
     */
    fun getNetWorkPath(fileId: String, ext: String): String {
        // 转为二进制
        val byteCode = java.lang.Long.toBinaryString(fileId.toLong())
        //获取时间位
        val time = byteCode.substring(0, 41)
        // 获取时间戳
        val timestamp = time.toLong(2)
        val date = Date(timestamp)
        val simpleDateFormatYMD = SimpleDateFormat("yyyyMMdd")
        val ymd = simpleDateFormatYMD.format(date)
        val simpleDateFormatHMSSss = SimpleDateFormat("HHmmssSSSS")
        val hm = simpleDateFormatHMSSss.format(date).toLong()
        val yhdhm = ymd + hm
        return  "/" + ymd + "/" + java.lang.Long.toHexString(hm) + "/" + java.lang.Long.toHexString(
            fileId.toLong()
        ) + "." + ext
    }

    /**
     * @param bytes
     * @return 将二进制数组转换为十六进制字符串  2-16
     */
    fun bin2HexStr(bytes: ByteArray): String {
        val result = StringBuilder()
        var hex = ""
        for (aByte in bytes) {
            //字节高4位
            hex = hexStr[aByte.toInt() and 0xF0 shr 4].toString()
            //字节低4位
            hex += hexStr[aByte.toInt() and 0x0F].toString()
            result.append(hex) //+" "
        }
        return result.toString()
    }

    fun String.getExtension(): String? {
        return FileUtil.extName(this).takeIf { it.isNotBlank() }
    }
}