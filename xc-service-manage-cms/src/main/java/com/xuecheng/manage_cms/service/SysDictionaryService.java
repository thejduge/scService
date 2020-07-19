package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//数据字典查询
@Service
public class SysDictionaryService {

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    //数据字典查询
    public SysDictionary getByType(String dType) {
        SysDictionary valueByDType = sysDictionaryRepository.findValueByDType(dType);
        //System.out.println("SysDictionary: "+valueByDType);
        return valueByDType;
    }
}
