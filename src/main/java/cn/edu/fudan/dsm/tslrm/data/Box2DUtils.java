package cn.edu.fudan.dsm.tslrm.data;

import cn.edu.fudan.dsm.tslrm.PLASegment;
import math.geom2d.Box2D;
import math.geom2d.polygon.Polygon2D;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-11-14
 * Time: 下午2:51
 * To change this template use File | Settings | File Templates.
 */
public class Box2DUtils {
    public static Box2D calcMaxBoundBoxByPolygonList(List<Polygon2D> polygon2DList) {
        Box2D ret = null;
        for (int i = 0; i < polygon2DList.size(); i++) {
            Polygon2D polygon2D = polygon2DList.get(i);
            Box2D boundingBox = polygon2D.boundingBox();
            if (ret == null) {
                ret = boundingBox;
            } else {
                double minX = Math.min(ret.getMinX(), boundingBox.getMinX());
                double maxX = Math.max(ret.getMaxX(), boundingBox.getMaxX());
                double minY = Math.min(ret.getMinY(), boundingBox.getMinY());
                double maxY = Math.max(ret.getMaxY(), boundingBox.getMaxY());

                ret = new Box2D(minX, maxX, minY, maxY);
            }

        }
        return ret;
    }

    public static Box2D[] splitBox2DEquallyByX(Box2D box2D) {
        Box2D[] ret = new Box2D[2];
        ret[0] = new Box2D(box2D.getMinX(), box2D.getMinX() + box2D.getWidth() / 2, box2D.getMinY(), box2D.getMaxY());
        ret[1] = new Box2D(box2D.getMinX() + box2D.getWidth() / 2, box2D.getMaxX(), box2D.getMinY(), box2D.getMaxY());
        return ret;
    }

    public static Box2D[] splitBox2DEquallyByY(Box2D box2D) {
        Box2D[] ret = new Box2D[2];
        ret[0] = new Box2D(box2D.getMinX(), box2D.getMaxX(), box2D.getMinY(), box2D.getMinY() + box2D.getHeight() / 2);
        ret[1] = new Box2D(box2D.getMinX(), box2D.getMaxX(), box2D.getMinY() + box2D.getHeight() / 2, box2D.getMaxY());
        return ret;
    }

    public static Box2D[] splitBox2DEquallyByLargeDimension(Box2D box2D)
    {
        if (box2D.getWidth() >= box2D.getHeight())
            return splitBox2DEquallyByX(box2D);
        else
            return splitBox2DEquallyByY(box2D);
    }

    public static Box2D calcMaxBoundBoxBySegmentList(List<PLASegment> segmentList) {
        Box2D ret = null;
        for (int i = 0; i < segmentList.size(); i++) {
            Polygon2D polygon2D = segmentList.get(i).getPolygonKB();
            Box2D boundingBox = polygon2D.boundingBox();
            if (ret == null) {
                ret = boundingBox;
            } else {
                double minX = Math.min(ret.getMinX(), boundingBox.getMinX());
                double maxX = Math.max(ret.getMaxX(), boundingBox.getMaxX());
                double minY = Math.min(ret.getMinY(), boundingBox.getMinY());
                double maxY = Math.max(ret.getMaxY(), boundingBox.getMaxY());

                ret = new Box2D(minX, maxX, minY, maxY);
            }
        }
        return ret;
    }
}
