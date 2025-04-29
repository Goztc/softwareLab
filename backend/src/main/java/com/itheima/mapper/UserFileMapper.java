package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.UserFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFileMapper extends BaseMapper<UserFile> {
    @Select("SELECT * FROM user_file WHERE id = #{fileId} AND user_id = #{userId}")
    UserFile findByIdAndUserId(Long fileId, Long userId);

    @Select("SELECT * FROM user_file WHERE folder_id = #{folderId} AND user_id = #{userId}")
    List<UserFile> findByFolderIdAndUserId(Long folderId, Long userId);
} 