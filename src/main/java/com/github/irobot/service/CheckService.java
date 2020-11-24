package com.github.irobot.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.irobot.util.SignUtil;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class CheckService {
    /**
     * @param publicKey
     * @param privateKey
     * @param imageUrl
     * @return pass-放行， forbid-封禁， check-人工审核
     * @throws Exception
     */
    public String check(String publicKey, String privateKey, String imageUrl) {
        String ucloudUrl = "http://api.uai.ucloud.cn/v1/image/scan";
        String appId = "uaicensor-ntrq5zcv";

        //图片绝对路径
        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        /**
         * 生成signature，首字母排序
         */
        String timestamp = System.currentTimeMillis() + "";
        SortedMap<Object, Object> packageParams = new TreeMap<>();
        packageParams.put("PublicKey", publicKey);
        packageParams.put("ResourceId", appId);
        packageParams.put("Timestamp", timestamp);
        packageParams.put("Url", imageUrl);
        String signature = null;
        try {
            signature = SignUtil.createSign(packageParams, privateKey);
        } catch (Exception e) {
            return null;
        }
        /**
         * 参数
         */
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("Scenes", "porn");
        param.add("Method", "url");
        param.add("Url", imageUrl);
        /**
         * headers 参数
         */
        headers.setContentType(MediaType.parseMediaType("multipart/form-data; charset=UTF-8"));
        headers.set("PublicKey", publicKey);
        headers.set("Signature", signature);
        headers.set("ResourceId", appId);
        headers.set("Timestamp", timestamp);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        ResponseEntity<String> responseEntity = rest.exchange(ucloudUrl, HttpMethod.POST, httpEntity, String.class);
        String body = responseEntity.getBody();
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.getInteger("RetCode") == 0) {
            return jsonObject.getJSONObject("Result").getJSONObject("Porn").getString("Suggestion");
        }
        return null;
    }
}
