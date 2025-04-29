package com.itheima.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.UserFolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFolderMapper extends BaseMapper<UserFolder> {
    @Insert("INSERT INTO user_folder(folder_name, parent_id, user_id, create_time, update_time) " +
            "VALUES(#{folderName}, #{parentId}, #{userId}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserFolder folder);

    @Select("SELECT * FROM user_folder WHERE user_id = #{userId} AND (parent_id = #{parentId} OR (#{parentId} IS NULL AND parent_id IS NULL)) " +
            "ORDER BY create_time DESC")
    List<UserFolder> findByUserIdAndParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    @Select("SELECT * FROM user_folder WHERE id = #{folderId} AND user_id = #{userId}")
    UserFolder findByIdAndUserId(Long folderId, Long userId);
} 