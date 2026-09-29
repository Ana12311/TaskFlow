package com.example.taskflow.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 字段自动填充。
 * 数据库里 created_at / updated_at 用 DEFAULT CURRENT_TIMESTAMP 生成，
 * 但 MP insert 后不会把 DB 默认值读回实体，导致 create 接口返回 createdAt=null。
 * 这里在 insert / update 时用 Java 直接填上时间戳。
 * 只对标注了 @TableField(fill = ...) 的字段生效，其余字段不受影响。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
