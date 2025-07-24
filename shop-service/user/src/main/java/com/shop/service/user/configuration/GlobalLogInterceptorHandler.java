package com.shop.service.user.configuration;

import com.alibaba.fastjson.JSONObject;
import com.shop.service.module.entity.LogEntity;
import com.shop.service.module.entity.Result;
import com.shop.service.module.feign.LogFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

public class GlobalLogInterceptorHandler implements HandlerInterceptor {


    @Autowired
    private LogFeignService logFeignService;



    public GlobalLogInterceptorHandler(LogFeignService logFeignService){
        this.logFeignService = logFeignService;
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");

        if(ip == null || ip.length() == 0 || "unknow".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");

        }

        if (ip == null || ip.length () == 0 || "unknown".equalsIgnoreCase (ip)) {
            ip = request.getHeader ("WL-Proxy-Client-IP");

        }

        if (ip == null || ip.length () == 0 || "unknown".equalsIgnoreCase (ip)) {
            ip = request.getRemoteAddr ();

            if (ip.equals ("127.0.0.1")) {
//根据网卡取本机配置的IP

                InetAddress inet = null;

                try {
                    inet = InetAddress.getLocalHost ();

                } catch (Exception e) {
                    e.printStackTrace ();

                }

                ip = inet.getHostAddress ();

            }

        }

// 多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割

        if (ip != null && ip.length () > 15) {
            if (ip.indexOf (",") > 0) {
                ip = ip.substring (0, ip.indexOf (","));

            }

        }

        return ip;
    }
    /**
     * 在整个请求结束之后被调用，也就是在DispatcherServlet 渲染了对应的视图之后执行（主要是用于进行资源清理工作）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // System.out.println("执行了TestInterceptor的afterCompletion方法");

        LogEntity l = new LogEntity();
        Map<String, String[]> parameterMap = request.getParameterMap();
        String httpMethod = request.getMethod();
        String path = request.getRequestURI();
        String ip = this.getIpAddress(request);
        String paramsJson = JSONObject.toJSONString(parameterMap);
        int status = response.getStatus();
        l.setIp(ip);
        l.setPath(path);
        l.setRequest(paramsJson);
        l.setStatusCode(status);
        l.setMethod(httpMethod);
        String token = request.getHeader("Authorization");
        if(token!=null){
            Jwt tokenObj = JwtHelper.decode(token.replace("Bearer ", "").trim());
            JSONObject jtoken = JSONObject.parseObject(tokenObj.getClaims());
            JSONObject userInfo = JSONObject.parseObject(jtoken.get("userInfo").toString());
            l.setUserId(Long.valueOf(userInfo.get("id").toString()));
            l.setUserAccount(userInfo.get("username").toString());
            l.setUserNickname(userInfo.get("nickname").toString());
        }
        //获取返回结果
        Object result = request.getAttribute("response");
        //转化为json
        String jsonResult = "";
        if(result != null) {
            jsonResult = JSONObject.toJSONString(result);
        }
        l.setResponse(jsonResult);
        l.setInsertTime(new Date());
        JSONObject res = logFeignService.insert(l);
        System.out.println(res);

        HttpSession session = request.getSession();
        String  s = (String)session.getAttribute("userInfo");
        JSONObject u = JSONObject.parseObject(s);
        System.out.println(u);

    }
}
