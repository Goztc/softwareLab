package com.itheima.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.Folder;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FolderMapper extends BaseMapper<Folder> {
    @Select("SELECT * FROM folder WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "children", column = "id",
                    many = @Many(select = "findByParentId"))
    })
    List<Folder> selectFolderTree(Long userId);

    @Select("SELECT * FROM folder WHERE parent_id = #{parentId}")
    List<Folder> findByParentId(Long parentId);
}