package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    ///根据账号查询用户的信息，返回用户扩展信息
    public XcUserExt getUserext(String username) {
        //根据用户账号查询用户信息
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser == null){
            return null;
        }
        //根据用户id查询用户权限
        String userId = xcUser.getId();
        List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(userId);
        XcUserExt xcUserExt = new XcUserExt();
        //拷贝属性
        BeanUtils.copyProperties(xcUser,xcUserExt);
        //用户id
        //String userId2 = xcUserExt.getId();
        //用户权限
        xcUserExt.setPermissions(xcMenuList);
        //查询用户所属的公司
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findXcCompanyUserByUserId(userId);
        if (xcCompanyUser != null){
            String companyId = xcCompanyUser.getCompanyId();
            xcUserExt.setCompanyId(companyId);
        }
        return xcUserExt;
    }
    //根据用户账号查询用户信息
    private XcUser findXcUserByUsername(String username) {
        return xcUserRepository.findXcUserByUsername(username);
    }
}
