//拦截器，用于限制某些请求的访问次数，防止恶意攻击；该方法会在Controller方法被调用之前被执行
package com.star.aspect;

import com.alibaba.fastjson.JSONObject;
import com.star.annotation.AccessLimit;
import com.star.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: SessionInterceptor
 */
@Component
public class SessionInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//      通过handler参数判断请求是否为HandlerMethod类型，如果是，则获取该方法上的AccessLimit注解，并从注解中获取seconds、maxCount和needLogin等属性值。
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (null == accessLimit) {
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();

            if (needLogin) {
                //判断是否登录
            }
            String ip=request.getRemoteAddr();
            String key = request.getServletPath() + ":" + ip ;
//            从redis中获取该key对应的计数器count
            Integer count = (Integer) redisTemplate.opsForValue().get(key);

            if (null == count || -1 == count) {
//              说明该IP地址第一次访问该接口，此时将count设置为1，并设置过期时间为seconds秒
                redisTemplate.opsForValue().set(key, 1,seconds, TimeUnit.SECONDS);
                return true;
            }

            if (count < maxCount) {
                count = count+1;
//                更新到redis中
                redisTemplate.opsForValue().set(key, count,0);
                return true;
            }

            if (count >= maxCount) {
//                response 返回 json 请求过于频繁请稍后再试
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                Response result = new Response<>();
                result.setCode("9999");
                result.setMsg("操作过于频繁,请稍后再提交");
                Object obj = JSONObject.toJSON(result);
                response.getWriter().write(JSONObject.toJSONString(obj));

                return false;
            }
        }

        return true;
    }
}
