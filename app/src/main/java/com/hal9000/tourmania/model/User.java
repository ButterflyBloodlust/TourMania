package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Users")
public class User {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int userId;

    @ColumnInfo(name = "username")
    private String username;

    public User() {}

    @Ignore
    public User(String username) {
        this.setUsername(username);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
