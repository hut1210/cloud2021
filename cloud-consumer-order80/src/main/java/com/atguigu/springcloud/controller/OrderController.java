package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import com.atguigu.springcloud.lb.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;

/**
 * @author huteng5
 * @version 1.0
 * @date 2021/8/13 17:15
 */
@RestController
@Slf4j
public class OrderController {

    //public static final String PAYMENT_URL = "http://localhost:8001";
    public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";
    @Resource
    private RestTemplate restTemplate;

    @Resource
    private LoadBalancer loadBalancer;

    @Resource
    private DiscoveryClient discoveryClient;

    @GetMapping("/consumer/payment/create")
    public CommonResult<Payment> create(Payment payment){
        return restTemplate.postForObject(PAYMENT_URL +"/payment/create",payment, CommonResult.class);
    }
    @GetMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPayment(@PathVariable("id") Long id){
        return restTemplate.getForObject(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);
    }

    @GetMapping("/consumer/payment/getForEntity/{id}")
    public CommonResult<Payment> getPayment2(@PathVariable("id") Long id)
    {
        ResponseEntity<CommonResult> entity = restTemplate.getForEntity(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);

        if(entity.getStatusCode().is2xxSuccessful()){
            return entity.getBody();
        }else{
            return new CommonResult<>(444,"操作失败");
        }
    }

    @GetMapping(value = "/consumer/payment/lb")
    /**
     * 使用自定义轮询规则需要注掉配置config中的@LoadBalanced
     */
    public String getPaymentLB()
    {
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        if(instances == null || instances.size() <= 0)
        {
            return null;
        }
        ServiceInstance serviceInstance = loadBalancer.instances(instances);
        URI uri = serviceInstance.getUri();
        System.out.println(uri);
        return restTemplate.getForObject(uri+"/payment/lb",String.class);

    }

    /**
     * 测试
     */
    @GetMapping("/consumer/yl/transfer")
    public CommonResult<Payment> transfer(){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate template =new RestTemplate();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.add("Accept", "application/json");//请求头
        headers.add("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjbHBzLWpkLWFnZW50X3lsIiwidXNlck5hbWUiOiJjbHBzLWpkLWFnZW50IiwiaWF0IjoxNjI5Nzk4MTQyfQ.yickp5DLNR4c-OU7KLTpvj7rjySLtv8HlPRnn-ZVlyk");//请求头
        String content = "测试asdfsafas";
        HttpEntity requestEntity = new HttpEntity(content);
        return restTemplate.postForObject("http://localhost:9001/api/yltransfer?content="+content,requestEntity, CommonResult.class);
    }

    /**
     * 测试
     */
    @GetMapping("/consumer/yl/transfer2")
    public CommonResult<Payment> transfer2(){
        HttpHeaders headers = new HttpHeaders();
        RestTemplate template =new RestTemplate();
        headers.add("Content-Type", "application/json;charset=UTF-8");
        headers.add("Accept", "application/json");//请求头
        headers.add("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjbHBzLWpkLWFnZW50X3lsIiwidXNlck5hbWUiOiJjbHBzLWpkLWFnZW50IiwiaWF0IjoxNjI5Nzk4MTQyfQ.yickp5DLNR4c-OU7KLTpvj7rjySLtv8HlPRnn-ZVlyk");//请求头
        MultiValueMap<String, String> postParameters = new LinkedMultiValueMap<String, String>();
        postParameters.add("content", "30001821");
        String data = "{\"content\":\"测试asdfsafas\"}";
        HttpEntity requestEntity = new HttpEntity(
                data, headers);
        return restTemplate.postForObject("http://localhost:9001/api/yl/transfer2",requestEntity, CommonResult.class);
    }
}
