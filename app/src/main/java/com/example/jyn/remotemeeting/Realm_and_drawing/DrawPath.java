package com.example.jyn.remotemeeting.Realm_and_drawing;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by JYN on 2018-01-05.
 *
 * 선을 한번 그릴 때 발생하는 일련의 정보들을 가지고 있는 클래스
 * (ACTION_DOWN -> ACTION_MOVE -> ACTION_UP)
 *
 * realm Object Server 의 클래스와 동일한 이름의 변수 타입과 이름을 가지고 있음
 */

public class DrawPath extends RealmObject {
    private boolean completed;
    private String color;
    private RealmList<DrawPoint> points;
    private String user_id;
    private int strokeWidth;
    private int strokeAlpha;

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public RealmList<DrawPoint> getPoints() {
        return points;
    }

    public void setPoints(RealmList<DrawPoint> points) {
        this.points = points;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeAlpha() {
        return strokeAlpha;
    }

    public void setStrokeAlpha(int strokeAlpha) {
        this.strokeAlpha = strokeAlpha;
    }

    public int getLength() {
        return points.size();
    }
}
