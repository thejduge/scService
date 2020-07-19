package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.SysDicthinary.SysDicthinaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/dictionary")
public class SysDicthinaryController implements SysDicthinaryControllerApi {

    @Autowired
    SysDictionaryService sysDictionaryService;

    /**
     * 数据字典查询
     * @param dType
     * @return
     */
    @Override
    @GetMapping(value = "/get/{dType}")
    public SysDictionary getByType(@PathVariable("dType") String dType) {
        //System.out.println("aaa"+dType);
        return sysDictionaryService.getByType(dType);
    }
}
