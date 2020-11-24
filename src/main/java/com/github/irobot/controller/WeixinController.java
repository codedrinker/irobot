package com.github.irobot.controller;

import com.github.developer.weapons.model.official.MessageTypeEnum;
import com.github.developer.weapons.model.official.OfficialAutoReplyMessage;
import com.github.developer.weapons.service.WechatOfficialService;
import com.github.irobot.service.CheckService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Controller
@Slf4j
public class WeixinController {

    @Autowired
    private WechatOfficialService wechatOfficialService;

    @Autowired
    private CheckService checkService;

    @Value("${weixin.token}")
    private String token;

    @Value("${ucloud.public.key}")
    private String publicKey;

    @Value("${ucloud.private.key}")
    private String privateKey;

    @RequestMapping(value = "/weixin/receive", method = RequestMethod.GET)
    public void receive(@RequestParam(value = "signature") String signature,
            @RequestParam(value = "timestamp") String timestamp,
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "echostr") String echostr,
            HttpServletResponse response) throws IOException {
        boolean valid = wechatOfficialService.isValid(signature, timestamp, nonce, token);
        PrintWriter writer = response.getWriter();
        if (valid) {
            writer.print(echostr);
        } else {
            writer.print("error");
        }
        writer.flush();
        writer.close();
    }

    @RequestMapping(value = "/weixin/receive", method = RequestMethod.POST)
    public void receive(@RequestParam(value = "signature") String signature,
            @RequestParam(value = "timestamp") String timestamp,
            @RequestParam(value = "nonce") String nonce,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        boolean valid = wechatOfficialService.isValid(signature, timestamp, nonce, token);
        PrintWriter writer = response.getWriter();
        if (!valid) {
            writer.print("error");
            writer.flush();
            writer.close();
            return;
        }
        try {
            Map<String, String> map = wechatOfficialService.toMap(request.getInputStream());
            if (map.get("MsgType").equals("image")) {
                String res = checkService.check(publicKey, privateKey, map.get("PicUrl"));
                OfficialAutoReplyMessage officialAutoReplyMessage =
                        OfficialAutoReplyMessage.build()
                                .withMsgtype(MessageTypeEnum.TEXT)
                                .withFromUserName(map.get("ToUserName"))
                                .withToUserName(map.get("FromUserName"));
                if (StringUtils.equals("forbid", res)) {
                    officialAutoReplyMessage.withContent("小哥，你的图片有点问题哦");
                } else {
                    officialAutoReplyMessage.withContent("骚年，你这图片刚刚的没问题");
                }
                writer.print(officialAutoReplyMessage.toXml());
                writer.flush();
                writer.close();
                return;
            }
        } catch (Exception e) {
            log.error("WeixinController receive error", e);
        }
        writer.print("success");
        writer.flush();
        writer.close();
    }
}
