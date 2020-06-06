package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygons2D;
import math.geom2d.polygon.SimplePolygon2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: MaYuanwen
 * Date: 12-11-25
 * Time: 下午3:05
 * To change this template use File | Settings | File Templates.
 * Using LowerBound
 */
public class PLAExtendDeepSearch2 {
    private  Point2D[] point2Ds;
    public double errorBound;
    public PLAExtendDeepSearch2(Point2D[] point2Ds, double errorBound) {
        this.point2Ds = point2Ds;
        this.errorBound = errorBound;
    }
    public PLAExtendDeepSearch2(){
    	
    }

    public double slope;
    public double intercept;

    public int search(List<PLASegment> segmentList, int startLength, double error) {
        int maxUpbound;
        int maxLength = startLength;
//        long timeforSort = System.currentTimeMillis();
        for (int i = 0; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            segment.idx = i;
            //compute the segment's lowerBound
            Point2D tempKB = segment.getPolygonKB().centroid();
            int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.x(), tempKB.y(), errorBound, segmentList.get(0).getLength());
            segment.setLowerBound(lowerBound);
            if(maxLength < lowerBound)
            	maxLength = lowerBound;
        }
        System.out.println("init MaxLength is " + maxLength);
//        sortSegmentList(segmentList);
//        timeforSort = System.currentTimeMillis() - timeforSort;
//        System.out.println("Time For Sort" + timeforSort);

        boolean[][] matrix = new boolean[segmentList.size()][segmentList.size()];
        int[] ubs = new int[segmentList.size()];

        //init matrix
        System.out.println("init matrix...");
        for (int i = 0; i < segmentList.size(); i++) {
//            System.out.println("matrix i = " + i);
            PLASegment segment = segmentList.get(i);
            for (int j = i; j < segmentList.size(); j++) {
                PLASegment plaSegment = segmentList.get(j);

                if (Polygons2D.intersection(segment.getPolygonKB(), plaSegment.getPolygonKB()).vertexNumber() > 0) {
                    matrix[i][j] = true;
                    matrix[j][i] = true;
                }
            }
        }

        maxUpbound = calcUbs(segmentList, matrix, ubs);
        //
        boolean b = adjustMatrix(matrix, ubs, startLength);
        while (b) {
            maxUpbound = calcUbs(segmentList, matrix, ubs);
            b = adjustMatrix(matrix, ubs, startLength);
        }

        List<PLASegment> stack = new ArrayList<PLASegment>();

        long maxStackSize = 0;
        long minStackSize = segmentList.size();

        Point2D maxKB = new Point2D(0, 0);
        long c = 0;
        
        PLASegment fakeSegment = new PLASegment();
        fakeSegment.idx = -1;
        fakeSegment.setStart(-10);
        fakeSegment.setEnd(-10);
        fakeSegment.setLength(0);
        fakeSegment.setPolygonKB(new SimplePolygon2D(TSPLAPointBoundKBMiner.X_INF, TSPLAPointBoundKBMiner.Y_INF));
        fakeSegment.totalLength = fakeSegment.getLength();
        fakeSegment.currentPolygon = fakeSegment.getPolygonKB();
        int currentLength = fakeSegment.getLength();
        stack.add(fakeSegment);
        int startIdx = 0;
        while (stack.size() > 0) {
            double e = (maxUpbound * 1.0 - maxLength) / maxUpbound;
            if (e <= error) {
                System.out.println("maxUpbound = " + maxUpbound);
                System.out.println("maxLength = " + maxLength);
                System.out.println("e = " + e);
                break;
            }

            PLASegment nextSegment = searchNext(stack, matrix, ubs, segmentList, startIdx);
            //add more segments into stack until end
            if (nextSegment != null) {
                PLASegment topSegment = stack.get(stack.size() - 1);
                stack.add(nextSegment);
                if (stack.size() > maxStackSize) {
                    maxStackSize = stack.size();
//                    System.out.println("maxStackSize = " + maxStackSize);
                }
                c++;
                if (c % 1000000 == 0) {
                    System.out.println("c = " + c);
                }

                //top    -------
                //next            -----
                if (nextSegment.getStart() > topSegment.getEnd()) {
                    currentLength = currentLength + nextSegment.getLength();
                } else if (nextSegment.getStart() <= topSegment.getEnd()) {
                    //top  --------
                    //next    -------
                    currentLength = currentLength + (nextSegment.getEnd() - topSegment.getEnd());
                }

                nextSegment.totalLength = currentLength;
                
				Point2D tempKB = stack.get(stack.size() - 1).currentPolygon.centroid();
                int lowerBound = SegmentUtils.verifyTrueLength(point2Ds, tempKB.x(), tempKB.y(), errorBound, segmentList.get(0).getLength());
                if(lowerBound > maxLength){
                	maxKB = tempKB;
                	maxLength = lowerBound;
                	//adjust matrix
                	boolean needProne = adjustMatrix2(matrix, ubs, maxLength);
                	while (needProne) {
                        maxUpbound = calcUbs(segmentList, matrix, ubs);
                        needProne = adjustMatrix2(matrix, ubs, maxLength);
                    }
//                	System.out.println("maxUpBound:" + maxUpbound + "!!!!!!!!!!");
                }
                
                //prone child
                //check the left upper bound
                int upperBound = currentLength + checkUpperBound(nextSegment.currentPolygon, segmentList, nextSegment.idx, nextSegment.idx + 1, matrix);
                if (upperBound == maxLength) {
//                	maxKB = stack.get(stack.size() - 1).currentPolygon.getCentroid();
//                	List<PLASegment> mergeList = SegmentUtils.mergeSegmentListByStartEnd(stack);
//                	maxList = new ArrayList<PLASegment>(mergeList);
                	maxKB = tempKB;
                	maxLength = lowerBound;
                	boolean needProne = adjustMatrix(matrix, ubs, maxLength);
                	while (needProne) {
                        maxUpbound = calcUbs(segmentList, matrix, ubs);
                        needProne = adjustMatrix(matrix, ubs, maxLength);
                    }
                	 //search the current stack to see if ubs[i] = 0;
                    for (int i = 1; i < stack.size(); i++) {
                        PLASegment segment = stack.get(i);
                        if (ubs[segment.idx] <= 0) {
                            //do pop until the segment
                            for (int j = stack.size() - 1; j > i; j--) {
                                stack.remove(j);
                            }
                            break;
                        }
                    }
					startIdx = segmentList.size();// trigger remove segment i
                } else if(upperBound < maxLength){
                	//do pop
                    startIdx = segmentList.size(); //force pop;
                }else{
                	 startIdx = nextSegment.idx + 1;
                }
            } else {
                //nextSegment == null
                PLASegment removed = stack.remove(stack.size() - 1);
                if (stack.size() < minStackSize) {
                    minStackSize = stack.size();
//                    System.out.println("minStackSize = " + minStackSize);
//                    System.out.println("removed = " + removed);
                }
                if (stack.size() == 0) {
                	System.out.println("Statck Size is 0!!!!!!!!!!!!!!!");
                    break;
                } else {
                    PLASegment topSegment = stack.get(stack.size() - 1);
                    currentLength = topSegment.totalLength;

                    if ((topSegment.getStart() <= removed.getStart()) && (topSegment.getEnd() >= removed.getStart()))  // intersected
                    {
                        startIdx = nextIdxOfStart(segmentList, topSegment.idx, topSegment.getEnd() + (topSegment.getLength() - 1));
                    } else {
                        startIdx = removed.idx + 1;
                    }
                }
            }
        }
        	System.out.println("maxLength = " + maxLength);
        	System.out.println("C = " + c + "!!!!!!!");
            
            if(maxKB != null){
            	System.out.println("k = " + maxKB.x());
                System.out.println("b = " + maxKB.y());
                int realLength = SegmentUtils.verifyTrueLength(point2Ds, maxKB.x(), maxKB.y(), errorBound, segmentList.get(0).getLength());
                System.out.println("RealLength with lowBound is " + realLength);
                this.slope = maxKB.x();
                this.intercept = maxKB.y();
            }
        return maxLength;
    }

    private boolean adjustMatrix(boolean[][] matrix, int[] ubs, long maxLength) {
        boolean needProne = false;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] <= maxLength) {
                if (ubs[i] > 0) {
//                    System.out.println("prone segment i = " + i);
                    ubs[i] = 0;
                    needProne = true;
                    for (int j = 0; j < ubs.length; j++) {
                        matrix[i][j] = false;
                        matrix[j][i] = false;
                    }
                }
            }
        }
        return needProne;
    }
    
    private boolean adjustMatrix2(boolean[][] matrix, int[] ubs, long maxLength) {
    	boolean needProne = false;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] < maxLength) {
                if (ubs[i] > 0) {
//                    System.out.println("prone segment i = " + i);
                    ubs[i] = 0;
                    needProne = true;
                    for (int j = 0; j < ubs.length; j++) {
                        matrix[i][j] = false;
                        matrix[j][i] = false;
                    }
                }
            }
        }
        return needProne;
    }

    private int calcUbs(List<PLASegment> segmentList, boolean[][] matrix, int[] ubs) {
        //calc ubs[i]
        PLASegment lastSegment;
        for (int i = 0; i < ubs.length; i++) {
            ubs[i] = 0;
            lastSegment = null;
            for (int j = 0; j < ubs.length; j++) {
                if (matrix[i][j]) {
                    PLASegment currentSegment = segmentList.get(j);

                    if (lastSegment == null) {
                        ubs[i] = ubs[i] + currentSegment.getLength();
                        lastSegment = currentSegment;
                    } else {
                        //check start,end intersection
                        //last       -----
                        //current           -----
                        if (currentSegment.getStart() > lastSegment.getEnd()) {
                            ubs[i] = ubs[i] + currentSegment.getLength();
                            lastSegment = currentSegment;
                        } else {
                            //last         -----
                            //current        ----
                            if (currentSegment.getEnd() > lastSegment.getEnd()) {
                                ubs[i] = ubs[i] + currentSegment.getEnd() - lastSegment.getEnd();
                                lastSegment = currentSegment;
                            } else {
                                //last          -------------
                                //current         ------
                                //do noting
                            }
                        }
                    }
                }
            }
        }

        long validCount = 0;
        for (int i = 0; i < ubs.length; i++) {
            if (ubs[i] > 0)
//                System.out.println("ub[" + i + "] = " + ubs[i] + "  " + segmentList.get(i));
            if (ubs[i] > 0) {
                validCount++;
            }
        }

//        System.out.println("validCount = " + validCount);
        int max = 0;
        for (int i = 0; i < ubs.length; i++) {
            int ub = ubs[i];
            if (ub > max) {
                max = ub;
            }
        }
//        System.out.println("maxUpbound = " + max);
        return max;
    }

    private int nextIdxOfStart(List<PLASegment> segmentList, int fromIdx, int start) {
        int ret = segmentList.size();
        for (int i = fromIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);
            if (segment.getStart() >= start) {
                ret = segment.idx;
                break;
            }
        }
        return ret;
    }

    private PLASegment searchNext(List<PLASegment> stack, boolean[][] matrix, int[] ubs, List<PLASegment> segmentList, int startIdx) {
        PLASegment topSegment = stack.get(stack.size() - 1);

        Polygon2D currentPolygon = topSegment.currentPolygon;
        for (int i = startIdx; i < segmentList.size(); i++) {
            PLASegment segment = segmentList.get(i);

            if (ubs[i] <= 0)
                continue;

            if (topSegment.idx >= 0)  //ignore the fake segment
                if (false == matrix[topSegment.idx][i])
                    continue;

            Polygon2D intersection = Polygons2D.intersection(currentPolygon, segment.getPolygonKB());
            if (intersection.vertexNumber() >= 0) {
                segment.currentPolygon = intersection;
                return segment;
            }
        }

        return null;
    }

    private int checkUpperBound(Polygon2D currentPolygon, List<PLASegment> segmentList, int currentIdx, int startIdx, boolean[][] matrix) {
        List<PLASegment> list = new ArrayList<PLASegment>();
        for (int i = startIdx; i < segmentList.size(); i++) {
            if (currentIdx >= 0) {
                if (matrix[currentIdx][i] == false)
                    continue;
            }

            PLASegment segment = segmentList.get(i);
            if (Polygons2D.intersection(currentPolygon, segment.getPolygonKB()).vertexNumber() > 0) {
                list.add(segment);
            }
        }

        return calcUpperBound(list, segmentList.get(currentIdx));
    }

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
    
    //sort the segmentList based on lowerbound
    public void sortSegmentList(List<PLASegment> segmentList){
    	//sort asc
        Collections.sort(segmentList, new Comparator<PLASegment>() {
            @Override
            public int compare(PLASegment o1, PLASegment o2) {
                return o2.getLowerBound() - o1.getLowerBound();
            }
        });
    }
}
