package com.peitianbao.www.controller;

import com.peitianbao.www.springframework.annontion.Controller;
import com.peitianbao.www.springframework.annontion.RequestMapping;
import com.peitianbao.www.springframework.annontion.RequestMethod;

/**
 * @author leg
 */
@Controller
public class ShopController {
    @RequestMapping(value = "/test", methodType = RequestMethod.GET)
    public String test() {
        System.out.println("ShopController 的 test 方法被调用！");
        return "Hello, World!";
    }


}
