package com.github.irobot.util;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class SignUtil {
    /**
     * sign签名
     */
    public static String createSign(SortedMap<Object, Object> packageParams,
                                    String privateKey) throws Exception {
        StringBuffer sb = new StringBuffer();
        Set es = packageParams.entrySet();
        Iterator it = es.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            String v = (String) entry.getValue();
            sb.append(k + v);
        }
        sb.append(privateKey);
        String sign = shaEncode(sb.toString());
        return sign;
    }

    public static String shaEncode(String inStr) throws Exception {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }

        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
}