package com.itheima.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.pojo.File;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    default List<File> selectByFolder(Long folderId) {
        return selectList(new LambdaQueryWrapper<File>()
                .eq(File::getFolderId, folderId));
    }
}