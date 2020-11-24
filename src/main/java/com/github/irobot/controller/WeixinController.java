package com.github.irobot.controller;

import com.github.developer.weapons.model.official.MessageTypeEnum;
import com.github.developer.weapons.model.official.OfficialAutoReplyMessage;
import com.github.developer.weapons.service.WechatOfficialService;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/weixin")
@Slf4j
public class WeixinController {

    @Autowired
    private WechatOfficialService wechatOfficialService;

    @Value("${weixin.token}")
    private String token;

    @RequestMapping(value = "/verify", method = RequestMethod.GET)
    public void verify(
            @RequestParam(value = "signature") String signature,
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

    @RequestMapping(value = "/receive", method = RequestMethod.POST)
    public void receive(
            @RequestParam(value = "signature") String signature,
            @RequestParam(value = "timestamp") String timestamp,
            @RequestParam(value = "nonce") String nonce,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
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
            String ToUserName = map.get("ToUserName");
            String FromUserName = map.get("FromUserName");
            String CreateTime = map.get("CreateTime");
            String MsgType = map.get("MsgType");
            String Content = map.get("Content");
            if (MsgType.equals("text")) {//判断消息类型是否是文本消息(text)
                String msg = OfficialAutoReplyMessage.build()
                        .withContent("您好，" + FromUserName + "\n我是：" + ToUserName
                                + "\n您发送的消息类型为：" + MsgType + "\n您发送的时间为" + CreateTime
                                + "\n 内容是：" + Content)
                        .withMsgtype(MessageTypeEnum.TEXT)
                        .withFromUserName(FromUserName)
                        .withToUserName(ToUserName)
                        .toXml();
                writer.print(msg); //返回转换后的XML字符串
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
