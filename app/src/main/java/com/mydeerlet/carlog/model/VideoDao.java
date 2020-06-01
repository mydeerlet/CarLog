package com.mydeerlet.carlog.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * @author myDeerlet
 * @date 2020/6/1
 * email：kuaileniaofei@163.com
 */

@Dao
public interface VideoDao {

    /**
     *  1. OnConflictStrategy.REPLACE：冲突策略是取代旧数据同时继续事务。
     *  2. OnConflictStrategy.ROLLBACK：冲突策略是回滚事务。
     *  3. OnConflictStrategy.ABORT：冲突策略是终止事务。
     *  4. OnConflictStrategy.FAIL：冲突策略是事务失败。
     *  5. OnConflictStrategy.IGNORE：冲突策略是忽略冲突。
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long save( Video videos);


    //只能传递对象昂,删除时根据Video中的主键 来比对的
    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Video... videos);


    //只能传递对象昂,删除时根据Video中的主键 来比对的
    @Delete
    void deleteUsers(Video... videos);

    @Query("SELECT * FROM videolist")
    List<Video> getAllVideo();

}
