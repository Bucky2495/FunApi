package com.ycy.project.model.dto.user;

/**
 * @Auther: ycy
 * @Date: 2023/5/15 16:59
 * @Description:
 */


import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户视图
 *
 * @TableName user
 */
@Data
public class UserDTO implements Serializable {
    private Long id;
    private String userName;
    private String userAccount;
    private String userAvatar;
    private Integer gender;
    private String userRole;
}