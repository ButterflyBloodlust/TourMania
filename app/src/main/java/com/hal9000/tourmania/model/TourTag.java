package com.hal9000.tourmania.model;

import com.hal9000.tourmania.rest_api.Exclude;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "TourTags",
        foreignKeys = @ForeignKey(entity = Tour.class,
                parentColumns = "tour_id_pk",
                childColumns = "tour_id",
                onDelete = ForeignKey.CASCADE),
        indices=@Index(value="tour_id"))
public class TourTag {
    @Exclude
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int tagId;

    @ColumnInfo(name = "tag")
    private String tag;

    @Exclude
    @ColumnInfo(name = "tour_id")
    private int tourId;

    public TourTag() {}

    @Ignore
    public TourTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public int getTourId() {
        return tourId;
    }

    public void setTourId(int tourId) {
        this.tourId = tourId;
    }
}
