package com.juzi.flymsg.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.juzi.flymsg.common.ErrorCode;
import com.juzi.flymsg.exception.BusinessException;
import com.juzi.flymsg.mapper.UserLoginInfoMapper;
import com.juzi.flymsg.model.dto.UserLoginRequest;
import com.juzi.flymsg.model.entity.UserLoginInfo;
import com.juzi.flymsg.model.vo.UserInfoVO;
import com.juzi.flymsg.service.UserLoginInfoService;
import com.juzi.flymsg.utils.ValidCheckUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

import java.nio.charset.StandardCharsets;

import static com.juzi.flymsg.constant.UserConstant.SALT;
import static com.juzi.flymsg.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author codejuzi
 * @description 针对表【userLoginInfo(用户登录信息表)】的数据库操作Service实现
 * @createDate 2023-04-02 20:28:12
 */
@Service
public class UserLoginInfoServiceImpl extends ServiceImpl<UserLoginInfoMapper, UserLoginInfo>
        implements UserLoginInfoService {

    @Override
    public Long userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 1、校验
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        ValidCheckUtil.commonLoginRegistryCheck(userAccount, userPassword);
        // 2、根据userAccount查询数据库
        LambdaQueryWrapper<UserLoginInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLoginInfo::getUserAccount, userAccount);
        UserLoginInfo userLoginInfo = this.getOne(queryWrapper);
        if (userLoginInfo == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号已经存在");
        }
        // 3、比较密码
        String encryptPasswordInDb = userLoginInfo.getUserPassword();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        if (!encryptPasswordInDb.equals(encryptPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码不正确");
        }
        // 4、保存用户登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, userLoginInfo);
        // 5、返回用户id
        return userLoginInfo.getUserId();
    }

    @Override
    public Boolean userLogout(HttpServletRequest request) {
        // 移除用户登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }


}




