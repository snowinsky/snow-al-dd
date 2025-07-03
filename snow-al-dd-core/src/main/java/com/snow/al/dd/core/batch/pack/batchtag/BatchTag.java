package com.snow.al.dd.core.batch.pack.batchtag;

public class BatchTag {

    /**
     * 使用 MD5将一个字符串转换为一个 32 位的字符串
     */
    public static String md5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(str.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte item : array) {
                sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("md5 error", e);
        }
    }

}
