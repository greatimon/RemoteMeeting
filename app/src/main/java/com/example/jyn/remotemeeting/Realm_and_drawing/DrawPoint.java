package com.example.jyn.remotemeeting.Realm_and_drawing;

import io.realm.RealmObject;

/**
 * Created by JYN on 2018-01-05.
 *
 * 캔버스에 드로잉할 때,
 * 선을 구성하는 점의 'x, y' 좌표 및 배경으로 깔려있는 전체 비트맵의 맨 위 y, 즉 0 값을 기준으로
 * 서피스뷰가 얼마만큼의 y 값만큼 밑으로 내려가서 뷰를 비추고 있는지에 대한 'top_y' 정보를 담는 클래스
 *
 *
 * realm Object Server 의 클래스와 동일한 이름의 변수 타입과 이름을 가지고 있음
 */

public class DrawPoint extends RealmObject {
    private double x;
    private double y;
    private int top_y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getTop_y() {
        return top_y;
    }

    public void setTop_y(int top_y) {
        this.top_y = top_y;
    }
}
