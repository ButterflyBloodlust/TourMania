package com.hal9000.tourmania.database;

import com.hal9000.tourmania.model.User;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDAO {
    @Query("SELECT * FROM Users")
    List<User> getUsers();

    @Query("SELECT * FROM Users WHERE user_id_pk = :userId")
    User getUserById(int userId);

    @Query("SELECT * FROM Users WHERE username = :username")
    User getUserByNickname(String username);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertUsers(List<User> users);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);
}
