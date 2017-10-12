package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.Box2DUtils;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import math.geom2d.polygon.SimplePolygon2D;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: MaYuanwen
 * Date: 1-24-2013
 * Time: 下午6:44
 * To change this template use File | Settings | File Templates.
 */
public class PLARegionSearch {
    static Logger logger = Logger.getLogger(PLARegionSearch.class);

    //to support multi points verify, for example 1,4,9  n*n
    public int verifyPointCountRadix = 1;

    private Point2D[] point2Ds;

    public PLARegionSearch(Point2D[] point2Ds) {
        this.point2Ds = point2Ds;
    }

    //记录最优解所能表示的最大长度
    public int finalLength = 0;
    public double slope;
    public double intercept;
    public int partitionNum = 0;        //?
    public double errorBound;
    public int maxLength = 0;           //current maxLength
    public int maxUpBound = Integer.MAX_VALUE;   //current maxUpbound
    public int countInsides = 0;                  //
    public StopWatch stopWatchInsides = new StopWatch();
    int plePolyNum = 0;
    //used by timer
    public int period = 1000;
    public int count = 0;
    public double curError = 1;
    public double baseUpBound = 0;
    public double finalError = 0;
    public StringBuilder builder1 = new StringBuilder("");
    public StringBuilder builder2 = new StringBuilder("");
    public StringBuilder builder3 = new StringBuilder("");

    public int calcUpperBound(List<PLASegment> segmentList, PLASegment startExcludeSegment) {
        int ret = 0;
        //the segmentList is sorted
        if (segmentList.size() <= 0)
            return ret;

        PLASegment lastSegment = startExcludeSegment;
//        ret = lastSegment.getLength();

        for (int i = 0; i < segmentList.size(); i++) {
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

    public Point2D searchByBox2DWithAccuracy(List<PLASegment> segmentList, double error, int piod, double bUpBound) {
        period = piod;
        Point2D ret = null;
        baseUpBound = bUpBound;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                double e = (baseUpBound * 1.0 - maxLength) / baseUpBound;
                double accuracy = 1 - e;
                int tempTime = count * period / 1000;
                builder1.append(tempTime + "\t");
                builder2.append(accuracy + "\t");
                count++;
            }

        }, 0, period);

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<PLASegment> intersectedPlaSegments = getIntersectedPlaSegments(segmentList, box2D);
        item.plaSegments = intersectedPlaSegments;
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
            if (queue.size() % 1000 < 2) {
                logger.debug("queue size = " + queue.size());
                logger.debug("maxUpbound = " + maxUpBound);
                logger.debug("maxLength = " + maxLength);
            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                finalError = 1 - e;
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                List<PLASegment> childIntersectedPlaSegments = getIntersectedPlaSegments(remove.plaSegments, childItem.box2D);
                childItem.plaSegments = childIntersectedPlaSegments;
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        timer.cancel();

        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DWithAccuracyInsides(List<PLASegment> segmentList, double error, int piod, double bUpBound) {
        period = piod;
        Point2D ret = null;
        baseUpBound = bUpBound;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                double e = (baseUpBound * 1.0 - maxLength) / baseUpBound;
                double accuracy = 1 - e;
                int tempTime = count * period / 1000;
                builder1.append(tempTime + "\t");
                builder2.append(accuracy + "\t");
                double tempError = 1 - curError;
                builder3.append(tempError + "\t");
                count++;
            }

        }, 0, period);

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());

        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
            if (queue.size() % 1000 < 2) {
                logger.debug("queue size = " + queue.size());
                logger.debug("maxUpbound = " + maxUpBound);
                logger.debug("maxLength = " + maxLength);
            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            curError = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (curError <= error) {
                finalError = 1 - curError;
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        timer.cancel();
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }
    
    public Point2D searchByBox2DWithBounLengthInsides(List<PLASegment> segmentList, double error, int piod) {
        period = piod;
        Point2D ret = null;

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {               
                int tempTime = count * period / 1000;
                builder1.append(tempTime + "\t");
                builder2.append(maxUpBound + "\t");                
                builder3.append(maxLength + "\t");
                count++;
            }

        }, 0, period);

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());

        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
            if (queue.size() % 1000 < 2) {
                logger.debug("queue size = " + queue.size());
                logger.debug("maxUpbound = " + maxUpBound);
                logger.debug("maxLength = " + maxLength);
            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            curError = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (curError <= error) {
                finalError = 1 - curError;
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        timer.cancel();
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Pair<Long, Pair<Long, Long>> searchByBox2DWithInsideCountTimes(List<PLASegment> segmentList, double error) {
        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);  // O(segment)

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());  // O(n)

        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);  // O(segment)
        item.upperBound = calcUpperBound(item.plaSegments);  // O(segment)

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        long cnt = 0;
        //loop when the queue is not empty
        while (queue.size() > 0) {
            cnt++;
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            curError = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (curError <= error) {
                finalError = 1 - curError;
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                //long startTime = System.currentTimeMillis();
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                //System.out.println("1: " + (System.currentTimeMillis() - startTime));
                //startTime = System.currentTimeMillis();
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                //System.out.println("2: " + (System.currentTimeMillis() - startTime));
                calcUpperBoundWatch.suspend();
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        //finalLength记录最优解
        finalLength = maxLength;
        return new Pair<>(cnt, new Pair<>(verifyWatch.getTime(), calcUpperBoundWatch.getTime()));
    }

    public Point2D searchByBox2DWithInside(List<PLASegment> segmentList, double error) {
        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());

        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();          

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            curError = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (curError <= error) {
                finalError = 1 - curError;
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();                  
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2D(List<PLASegment> segmentList, double error) {
        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<PLASegment> intersectedPlaSegments = getIntersectedPlaSegments(segmentList, box2D);
        item.plaSegments = intersectedPlaSegments;
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
//            if (queue.size() % 1000 < 2){
//            	logger.debug("queue size = " + queue.size());
//            	logger.debug("maxUpbound = " + maxUpBound);
//            	logger.debug("maxLength = " + maxLength);
//            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                List<PLASegment> childIntersectedPlaSegments = getIntersectedPlaSegments(remove.plaSegments, childItem.box2D);
                childItem.plaSegments = childIntersectedPlaSegments;
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;
                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);
        }
        logger.debug("maxQueueLength = " + maxQueueLength);
        logger.debug("calcUpperBoundCount = " + calcUpperBoundCount);
        logger.debug("verifyCount = " + verifyCount);
        logger.debug("verifyWatch.getTime() = " + verifyWatch.getTime());
        logger.debug("calcUpperBoundWatch.getTime() = " + calcUpperBoundWatch.getTime());
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DInsidesPer100(List<PLASegment> segmentList, double error, String fileName) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        String line = "";

        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;

                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

                if (calcUpperBoundCount % 100 == 0) {
                    long tempLowTime = verifyWatch.getTime();
                    long tempUpTime = calcUpperBoundWatch.getTime();
                    int tempPolyNum = plePolyNum / 100;
                    line += calcUpperBoundCount + "\t" + tempLowTime + "\t" + tempUpTime + "\t" + tempPolyNum;
                    pw.println(line);
                    verifyWatch.reset();
                    verifyWatch.start();
                    verifyWatch.suspend();
                    calcUpperBoundWatch.reset();
                    calcUpperBoundWatch.start();
                    calcUpperBoundWatch.suspend();
                    plePolyNum = 0;
                    line = "";
                }
            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        pw.close();
        logger.debug("maxQueueLength = " + maxQueueLength);
        logger.debug("calcUpperBoundCount = " + calcUpperBoundCount);
        logger.debug("verifyCount = " + verifyCount);
        logger.debug("verifyWatch.getTime() = " + verifyWatch.getTime());
        logger.debug("calcUpperBoundWatch.getTime() = " + calcUpperBoundWatch.getTime());
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DPerLengthInsides(List<PLASegment> segmentList, double error, String fileName, int perLength) throws IOException {
    	PrintWriter pw = new PrintWriter(new FileWriter(new File(fileName)));
        StringBuilder lengthLine = new StringBuilder("");
        StringBuilder upBoundTimeLine = new StringBuilder("");

        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<Boolean> initInsides = new ArrayList<Boolean>();
        for (int i = 0; i < segmentList.size(); i++) {
            initInsides.add(false);
        }
        getIntersectedPlaSegmentWithInside(segmentList, initInsides, box2D, item);
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                getIntersectedPlaSegmentWithInside(remove.plaSegments, remove.isInsides, childItem.box2D, childItem);
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;

                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

                if (calcUpperBoundCount % perLength == 0) {
                    long tempUpTime = calcUpperBoundWatch.getTime() / 1000;
                    lengthLine.append(calcUpperBoundCount + "\t");
                    upBoundTimeLine.append(tempUpTime + "\t");
                    verifyWatch.reset();
                    verifyWatch.start();
                    verifyWatch.suspend();
                    calcUpperBoundWatch.reset();
                    calcUpperBoundWatch.start();
                    calcUpperBoundWatch.suspend();
                }
            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        lengthLine.append(calcUpperBoundCount);
        upBoundTimeLine.append(calcUpperBoundWatch.getTime() / 1000);
        pw.println(lengthLine);
        pw.println(upBoundTimeLine);
        pw.close();
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DPer100(List<PLASegment> segmentList, double error, String fileName) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        String line = "";

        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<PLASegment> intersectedPlaSegments = getIntersectedPlaSegments(segmentList, box2D);
        item.plaSegments = intersectedPlaSegments;
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
            if (queue.size() % 1000 < 2) {
                logger.debug("queue size = " + queue.size());
                logger.debug("maxUpbound = " + maxUpBound);
                logger.debug("maxLength = " + maxLength);
            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                List<PLASegment> childIntersectedPlaSegments = getIntersectedPlaSegments(remove.plaSegments, childItem.box2D);
                childItem.plaSegments = childIntersectedPlaSegments;
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;

                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

                if (calcUpperBoundCount % 100 == 0) {
                    long tempLowTime = verifyWatch.getTime();
                    long tempUpTime = calcUpperBoundWatch.getTime();
                    int tempPolyNum = plePolyNum / 100;
                    line += calcUpperBoundCount + "\t" + tempLowTime + "\t" + tempUpTime + "\t" + tempPolyNum;
                    pw.println(line);
                    verifyWatch.reset();
                    verifyWatch.start();
                    verifyWatch.suspend();
                    calcUpperBoundWatch.reset();
                    calcUpperBoundWatch.start();
                    calcUpperBoundWatch.suspend();
                    plePolyNum = 0;
                    line = "";
                }
            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }
        pw.close();
        logger.debug("maxQueueLength = " + maxQueueLength);
        logger.debug("calcUpperBoundCount = " + calcUpperBoundCount);
        logger.debug("verifyCount = " + verifyCount);
        logger.debug("verifyWatch.getTime() = " + verifyWatch.getTime());
        logger.debug("calcUpperBoundWatch.getTime() = " + calcUpperBoundWatch.getTime());
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    public Point2D searchByBox2DPerLength(List<PLASegment> segmentList, double error, String fileName, int perLength) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        StringBuilder lengthLine = new StringBuilder("");
        StringBuilder upBoundTimeLine = new StringBuilder("");

        Point2D ret = null;

        List<QueueItem> queue = new ArrayList<QueueItem>();
        Box2D box2D = Box2DUtils.calcMaxBoundBoxBySegmentList(segmentList);

        QueueItem item = new QueueItem();
        item.box2D = box2D;
        double k = box2D.getMinX() + box2D.getWidth() / 2;
        double b = box2D.getMinY() + box2D.getHeight() / 2;

        item.k = k;
        item.b = b;
        item.lowerBound = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
        List<PLASegment> intersectedPlaSegments = getIntersectedPlaSegments(segmentList, box2D);
        item.plaSegments = intersectedPlaSegments;
        item.upperBound = calcUpperBound(item.plaSegments);

        queue.add(item);

        StopWatch verifyWatch = new StopWatch();
        verifyWatch.start();
        verifyWatch.suspend();
        StopWatch calcUpperBoundWatch = new StopWatch();
        calcUpperBoundWatch.start();
        calcUpperBoundWatch.suspend();
        int verifyCount = 0;
        int calcUpperBoundCount = 0;
        maxLength = item.lowerBound;
        maxUpBound = item.upperBound;
        int maxQueueLength = 0;
        boolean needPrune = false;

        //loop when the queue is not empty
        while (queue.size() > 0) {
            if (queue.size() > maxQueueLength)
                maxQueueLength = queue.size();
//            if (queue.size() % 1000 < 2){
//            	logger.debug("queue size = " + queue.size());
//            	logger.debug("maxUpbound = " + maxUpBound);
//            	logger.debug("maxLength = " + maxLength);
//            }

            maxUpBound = queue.get(0).upperBound;   //count > 0 and get(0) is safe
//            logger.debug("maxUpBound = " + maxUpBound);
//            logger.debug("maxLength = " + maxLength);
            //check error bound
            double e = (maxUpBound * 1.0 - maxLength) / maxUpBound;
            if (e <= error) {
                break;
            }

            //pop stack,remove first
            QueueItem remove = queue.remove(0);

            Box2D[] childBoxes = Box2DUtils.splitBox2DEquallyByLargeDimension(remove.box2D);
            partitionNum++;

            for (Box2D childBox : childBoxes) {
                QueueItem childItem = new QueueItem();
                childItem.box2D = childBox;
                //get verify points to test
                Point2D[] verifyPoints = getVerifyPoints(childBox, verifyPointCountRadix);
                int maxTrueLength = -1;
                for (int i = 0; i < verifyPoints.length; i++) {
                    Point2D verifyPoint = verifyPoints[i];
                    //get the center point of box
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                    k = verifyPoint.getX();
                    b = verifyPoint.getY();
                    verifyWatch.resume();
                    int trueLength = SegmentUtils.verifyTrueLength(point2Ds, k, b, errorBound, segmentList.get(0).getLength());
                    verifyWatch.suspend();
                    verifyCount++;
                    if (trueLength > maxTrueLength) {
                        maxTrueLength = trueLength;
                        childItem.k = k;
                        childItem.b = b;
                        childItem.lowerBound = trueLength;
                    }
                }

                calcUpperBoundWatch.resume();
//                childItem.upperBound = calcUpperBoundByBox2D(segmentList, childItem.box2D);
                List<PLASegment> childIntersectedPlaSegments = getIntersectedPlaSegments(remove.plaSegments, childItem.box2D);
                childItem.plaSegments = childIntersectedPlaSegments;
                childItem.upperBound = calcUpperBound(childItem.plaSegments);
                calcUpperBoundWatch.suspend();
                calcUpperBoundCount++;

                //lowerBound 是一个真实的解，使用MaxLength表示当前最大的一个真实解
                if (childItem.lowerBound > maxLength) {
                    maxLength = childItem.lowerBound;
                    ret = new Point2D(childItem.k, childItem.b);
                    needPrune = true;
                }
                //need test later
                if (childItem.upperBound > maxLength)
                    queue.add(childItem);

                if (calcUpperBoundCount % perLength == 0) {
                    long tempUpTime = calcUpperBoundWatch.getTime() / 1000;
                    lengthLine.append(calcUpperBoundCount + "\t");
                    upBoundTimeLine.append(tempUpTime + "\t");
                    verifyWatch.reset();
                    verifyWatch.start();
                    verifyWatch.suspend();
                    calcUpperBoundWatch.reset();
                    calcUpperBoundWatch.start();
                    calcUpperBoundWatch.suspend();
                }
            }
            //sort queue and prone some box2d
            //sort queue by upperBound descending
            //prone item whose upperBound less and equal than maxLength
            //当产生一个新的更优解时应该对已加入队列的Box进行裁剪，剔除upperBound小于或等于maxLength的Box
            if (needPrune) {
                for (int i = queue.size() - 1; i >= 0; i--) {
                    if (queue.get(i).upperBound <= maxLength) {
                        queue.remove(i);
                    }
                }
                needPrune = false;
            }
            //将队列中的Box按照upperBound排序,默认为upperBound越大，包含最优解的可能性就越大
            //sort queue by upperBound descending
            Collections.sort(queue);

            if (queue.size() > 100000) {
                logger.debug("Queue Size = " + queue.size());
            }
        }

        lengthLine.append(calcUpperBoundCount);
        upBoundTimeLine.append(calcUpperBoundWatch.getTime() / 1000);
        pw.println(lengthLine);
        pw.println(upBoundTimeLine);
        pw.close();
        //finalLength记录最优解
        finalLength = maxLength;
        return ret;
    }

    private Point2D[] getVerifyPoints(Box2D box2D, int verifyPointCountRadix) {
        Point2D[] ret = new Point2D[verifyPointCountRadix * verifyPointCountRadix];
        double xStep = box2D.getWidth() / (verifyPointCountRadix + 1);
        double yStep = box2D.getHeight() / (verifyPointCountRadix + 1);
        for (int i = 0; i < verifyPointCountRadix; i++) {
            for (int j = 0; j < verifyPointCountRadix; j++) {
//                    k = childBox.getMinX() + childBox.getWidth() / 2;
//                    b = childBox.getMinY() + childBox.getHeight() / 2;
                double k = box2D.getMinX() + xStep * (i + 1);
                double b = box2D.getMinY() + yStep * (j + 1);

                ret[i * verifyPointCountRadix + j] = new Point2D(k, b);
            }
        }

        return ret;
    }

    @Deprecated
    public int calcUpperBoundByBox2D(List<PLASegment> segmentList, Box2D box2D) {
        List<PLASegment> list = getIntersectedPlaSegments(segmentList, box2D);
        return calcUpperBound(list);
    }

    public int calcUpperBound(List<PLASegment> list) {
        int ret = 0;
        if (list.size() > 0) {
            ret = list.get(0).getLength() + calcUpperBound(list, list.get(0));
        }
        return ret;
    }

    public List<PLASegment> getIntersectedPlaSegments(List<PLASegment> segmentList, Box2D box2D) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        Polygon2D p = new SimplePolygon2D(new double[]{box2D.getMinX(), box2D.getMaxX(), box2D.getMaxX(), box2D.getMinX()}, new double[]{box2D.getMinY(), box2D.getMinY(), box2D.getMaxY(), box2D.getMaxY()});
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            try {
                Polygon2D intersection = Polygon2DUtils.intersection(p, segment.getPolygonKB());
                plePolyNum++;
                if (intersection.getVertexNumber() > 0) {
                    list.add(segment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return list;
    }

    /**
     * get segments intersect with Box2D(R01)
     *
     * @param segmentList   R0's intersection segment list
     * @param removeInsides R0's include
     * @param box2D         R01
     * @param item
     */
    public void getIntersectedPlaSegmentWithInside(List<PLASegment> segmentList, List<Boolean> removeInsides, Box2D box2D, QueueItem item) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        List<Boolean> insides = new ArrayList<Boolean>();

        Polygon2D p = new SimplePolygon2D(new double[]{box2D.getMinX(), box2D.getMaxX(), box2D.getMaxX(), box2D.getMinX()}, new double[]{box2D.getMinY(), box2D.getMinY(), box2D.getMaxY(), box2D.getMaxY()});
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (removeInsides.get(i)) {     //deal with surround polygon
                list.add(segment);          //the surround polygon always surround child
                insides.add(true);
                countInsides++;
                continue;                 //for next segment
            }
            try {                         //for normal polygon
                Polygon2D intersection = Polygon2DUtils.intersection(p, segment.getPolygonKB());
                plePolyNum++;
                if (intersection.getVertexNumber() > 0) {     //if intersection
                    list.add(segment);                        //add segment
                    double area1 = intersection.getArea();
                    double area2 = p.getArea();             //deal with surround
                    if (Math.abs(area1 - area2) / area2 < 0.000005) {                       //surround
                        insides.add(true);
                    } else {
                        insides.add(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        item.plaSegments = list;       //the intersection segment list for item
        item.isInsides = insides;      //the surround segment boolean matrix for item,the length of matrix is the length of intersection segment list
    }
}

class QueueItem implements Comparable<QueueItem> {
    Box2D box2D;   //rectangle Rx
    double k;
    double b;
    int upperBound;
    int lowerBound;  //True Length
    List<PLASegment> plaSegments;  //intersection segment list
    List<Boolean> isInsides;       //inside  or not

    @Override
    public int compareTo(QueueItem o) {
        //default sort by desc
        int upper = o.upperBound - this.upperBound;
        if (upper != 0)    //order by upperBound desc first
            return upper;
        else
            return o.lowerBound - this.lowerBound; //order by lowerBound desc
//        	return this.lowerBound - o.lowerBound; //order by lowerBound incr
    }
}
