package cn.edu.fudan.dsm.tslrm.data;

import cn.edu.fudan.dsm.tslrm.PLASegment;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-27
 * Time: 下午2:56
 * To change this template use File | Settings | File Templates.
 */
public class SegmentUtils {

    public static List<PLASegment> filter(List<PLASegment> orgList,int minLength,int maxLength)
    {
        List<PLASegment> ret = new ArrayList<PLASegment>();

        for (int i = 0; i < orgList.size(); i++) {
            PLASegment segment = orgList.get(i);
            if ((segment.getLength() >= minLength) && (segment.getLength()<=maxLength))
                ret.add(segment);
        }

        return ret;
    }

    /**
     * sort segment list by length asc
     *
     * @param segments
     */
    public static void sortSegmentList(List<PLASegment> segments) {
        //sort asc
        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o1.getLength() - o2.getLength();
            }
        });
    }

    public static void sortSegmentListByStartEnd(List<PLASegment> segments) {
        //sort asc
        Collections.sort(segments, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                if (o1.getStart() != o2.getStart())
                    return o1.getStart() - o2.getStart();
                else
                    return o1.getEnd() - o2.getEnd();
            }
        });
    }

    public static List<PLASegment> mergeSegmentListByStartEnd(List<PLASegment> plaSegments) {
        List<PLASegment> ret = new ArrayList<PLASegment>();

        PLASegment segment = null;
        for (int i = 0; i < plaSegments.size(); i++) {
            PLASegment nextSegment = plaSegments.get(i);
            if (segment == null) {
                segment = new PLASegment();
                segment.setStart( nextSegment.getStart());
                segment.setEnd(nextSegment.getEnd());
                segment.setPolygonKB(nextSegment.getPolygonKB());
                segment.setLength(nextSegment.getLength());
            } else {
                if (nextSegment.getStart() <= (segment.getEnd())) {
                    segment.setEnd(nextSegment.getEnd());
                    segment.setLength(segment.getLength() + (nextSegment.getEnd()-segment.getEnd()));
                    segment.setPolygonKB(Polygons2D.intersection(segment.getPolygonKB(), nextSegment.getPolygonKB()));
                } else {
                    ret.add(segment);
                    segment = new PLASegment();
                    segment.setStart( nextSegment.getStart());
                    segment.setEnd(nextSegment.getEnd());
                    segment.setPolygonKB(nextSegment.getPolygonKB());
                    segment.setLength(nextSegment.getLength());
//                    segment = nextSegment;
                }
            }
        }

        ret.add(segment);

        return ret;
    }

    public static int verifyTrueLength(Point2D[] point2Ds, double k, double b,double errorBound, int minSegLength) {
        int ret = 0;
        int consecutiveLength  = 0;
        int start = -1;
        int end = -1;
        for (int i = 0; i < point2Ds.length; i++) {
            double x = point2Ds[i].x();
            double y = point2Ds[i].y();
            double estimate_y = k * x + b;
            if (Math.abs(estimate_y - y) <= errorBound)
            {
                if (start < 0) start = i;
                consecutiveLength ++;
                if (consecutiveLength >= minSegLength)
                {
                    end = i;
                }
            }
            else
            {
                if (consecutiveLength >= 3)
                {
//                    System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
                    ret += consecutiveLength;
                }
                consecutiveLength = 0;

                //finish this segment
                start = -1;
            }
        }
        if (consecutiveLength >= 3)
        {
//            System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
            ret += consecutiveLength;
        }

        return ret;
    }

    public static Set<Integer> verifyTrueLengthReturnPoints(Point2D[] point2Ds, double k, double b,double errorBound, int minSegLength) {
        Set<Integer> ret = new HashSet<Integer>();

        int maxLength = 0;
        int consecutiveLength  = 0;
        int start = -1;
        int end = -1;
        for (int i = 0; i < point2Ds.length; i++) {
            double x = point2Ds[i].x();
            double y = point2Ds[i].y();
            double estimate_y = k * x + b;
            if (Math.abs(estimate_y - y) <= errorBound)
            {
                if (start < 0) start = i;
                consecutiveLength ++;
                if (consecutiveLength >= minSegLength)
                {
                    end = i;
                }
            }
            else
            {
                if (consecutiveLength >= 3)
                {
//                    System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
                    maxLength += consecutiveLength;
                    for (int p = start; p <= end; p++) {
                          ret.add(p);
                    }
                }
                consecutiveLength = 0;

                //finish this segment
                start = -1;
            }
        }
        if (consecutiveLength >= 3)
        {
//            System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
            for (int p = start; p <= end; p++) {
                ret.add(p);
            }
            maxLength += consecutiveLength;
        }

        return ret;
    }

    /**
     *
     * @param polygon
     * @param segmentList is ordered by start and end
     * @param startIdx
     * @param endIdx
     * @return
     */
    public static int calcUpperBound(Polygon2D polygon,List<PLASegment> segmentList,int startIdx, int endIdx) {
        int ret = 0;
        //the segmentList is sorted
        if (segmentList.size() <= 0)
            return ret;

        PLASegment lastSegment = null;

        for (int i = startIdx; i < endIdx; i++) {
            PLASegment currentSegment = segmentList.get(i);
            //last   --------
            //current            ------
            if (currentSegment.getStart() > lastSegment.getEnd()) {
                ret = ret + currentSegment.getLength();
                lastSegment = currentSegment;
            } else {
                //last         -----
                //current        ----
                if (currentSegment.getEnd() > lastSegment.getEnd()) {
                    ret = ret + currentSegment.getEnd() - lastSegment.getEnd();
                    lastSegment = currentSegment;
                } else {
                    //last          -------------
                    //current         ------
                    //do noting
                }
            }
        }

        return ret;
    }

    //add segmentList to restrict the point2Ds
    public static int verifyTrueLength(Point2D[] point2Ds, double k, double b, double errorBound, int minSegLength, List<PLASegment> segmentList) {
        int ret = 0;
        int consecutiveLength  = 0;
        int start = -1;
        int end = -1;
        for (int i = 0; i < point2Ds.length; i++) {
            double x = point2Ds[i].x();
            double y = point2Ds[i].y();
            double estimate_y = k * x + b;
            if ((Math.abs(estimate_y - y) <= errorBound) && (includeInSegmentList(segmentList,i)))       //add SegmentList check to support multi linear model detected
            {
                if (start < 0) start = i;
                consecutiveLength ++;
                if (consecutiveLength >= minSegLength)
                {
                    end = i;
                }
            }
            else
            {
                if (consecutiveLength >= 3)
                {
//                    System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
                    ret += consecutiveLength;
                }
                consecutiveLength = 0;

                //finish this segment
                start = -1;
            }
        }
        if (consecutiveLength >= 3)
        {
//            System.out.println("[" + start + "," + end + "] length=" + consecutiveLength);
            ret += consecutiveLength;
        }

        return ret;

    }

    private static boolean includeInSegmentList(List<PLASegment> segmentList, int i) {
        for (int j = 0; j < segmentList.size(); j++) {
            PLASegment plaSegment = segmentList.get(j);
            if (plaSegment.getStart() <= i && plaSegment.getEnd() >=i)
                return true;
        }
        return false;
    }
}
