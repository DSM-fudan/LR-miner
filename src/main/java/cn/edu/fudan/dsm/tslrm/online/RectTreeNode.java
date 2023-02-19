package cn.edu.fudan.dsm.tslrm.online;

import cn.edu.fudan.dsm.tslrm.PLARegionSearch;
import cn.edu.fudan.dsm.tslrm.PLASegment;
import cn.edu.fudan.dsm.tslrm.TSPLAPointBoundKBMiner;
import cn.edu.fudan.dsm.tslrm.data.Box2DUtils;
import cn.edu.fudan.dsm.tslrm.data.ClimateDATAUtils;
import cn.edu.fudan.dsm.tslrm.data.SegmentUtils;
import math.geom2d.Box2D;
import math.geom2d.Point2D;
import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.Polygon2DUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RectTreeNode {
    static public int min_seg_length = 3;
    static public int window_length =  10000;
    static public RectTreeNode bsf_node;
    static public int bsf_len = 0;
    static public double error_bound = 0.1;

    static private RectTreeNode root;
    static private List<PLASegment>cur_valid_segs;
    private ArrayList<Boolean> isIntersection = new ArrayList<>();
    private ArrayList<Boolean>   isSurround = new ArrayList<>();
    private ArrayList<Boolean>   isHit = new ArrayList<>();
    private boolean split_axis; // true for x, false for y
    private double split_point;
    private Box2D box;
    private int upper_bound;
    private int len_mid_point;
    private RectTreeNode left, right, parent;

    public RectTreeNode(){}

    public RectTreeNode(RectTreeNode _parent, boolean is_left){
        if(_parent.split_axis){
            if(is_left)
                box = new Box2D(_parent.box.getMinX(), _parent.split_point, _parent.box.getMinY(), _parent.box.getMaxY());
            else
                box = new Box2D(_parent.split_point, _parent.box.getMaxX(),_parent.box.getMinY(), _parent.box.getMaxY());
        }else{
            if(is_left)
                box = new Box2D(_parent.box.getMinX(), _parent.box.getMaxX(),_parent.box.getMinY(), _parent.split_point);
            else
                box = new Box2D(_parent.box.getMinX(), _parent.box.getMaxX(), _parent.split_point, _parent.box.getMaxY());
        }

        parent = _parent;
        double mid_x  = (box.getMaxX() + box.getMinX()) / 2.0, mid_y  = (box.getMaxY() + box.getMinY()) / 2.0;
        Point2D mid_point = new Point2D(mid_x, mid_y);
        ArrayList<PLASegment>lb_segs = new ArrayList<>();
        ArrayList<PLASegment>ub_segs = new ArrayList<>();

        for(int i=0;i<cur_valid_segs.size();++i){
            PLASegment segment = cur_valid_segs.get(i);
            if(parent.isSurround.get(i)){
                isSurround.add(true);
                isIntersection.add(true);
                isHit.add(true);
                ub_segs.add(segment);
                lb_segs.add(segment);
                continue;
            }
            if(!parent.isIntersection.get(i)){
                isSurround.add(false);
                isIntersection.add(false);
                isHit.add(false);
                continue;
            }
            Polygon2D segment_poly = cur_valid_segs.get(i).getPolygonKB();
            Polygon2D intersect = Polygon2DUtils.intersection(box.asRectangle(), segment_poly);
            if(intersect.getVertexNumber() > 0){
                isIntersection.add(true);
                ub_segs.add(segment);
                Collection<Point2D> vertices = box.vertices();
                boolean res = true;
                for(Point2D vertex:vertices){
                    if(!segment_poly.contains(vertex)){
                        res = false;
                        break;
                    }
                }
                isSurround.add(res);
                if(segment_poly.contains(mid_point)) {
                    isHit.add(true);
                    lb_segs.add(segment);
                }else   isHit.add(false);
            }else{
                isIntersection.add(false);
                isSurround.add(false);
                isHit.add(false);
            }
        }

        upper_bound = PLARegionSearch.calcUpperBound(ub_segs);
        len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
        if(bsf_len >= upper_bound)  return;
        if(len_mid_point > bsf_len){
            bsf_len = len_mid_point;
            bsf_node = this;
        }
    }

    private void initialize(){
        box = Box2DUtils.calcMaxBoundBoxBySegmentList(cur_valid_segs);
        parent = null;
        double mid_x  = (box.getMaxX() + box.getMinX()) / 2.0, mid_y  = (box.getMaxY() + box.getMinY()) / 2.0;
        Point2D mid_point = new Point2D(mid_x, mid_y);
        ArrayList<PLASegment>lb_segs = new ArrayList<>();
        for(PLASegment segment:cur_valid_segs){
            isIntersection.add(true);
            isSurround.add(false);
            if(segment.getPolygonKB().contains(mid_point)) {
                isHit.add(true);
                lb_segs.add(segment);
            }else   isHit.add(false);
        }
        upper_bound = PLARegionSearch.calcUpperBound(cur_valid_segs);
        len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
        if(bsf_len >= upper_bound)  return;
        if(len_mid_point > bsf_len){
            bsf_len = len_mid_point;
            bsf_node = this;
        }
        if(len_mid_point < upper_bound){
            split();
        }
    }

    private void split(){
        if(box.getHeight() > box.getWidth()){
            // split on y
            split_axis = false;
            split_point = (box.getMaxY() + box.getMinY()) / 2.0;
        }else{
            // split on x;
            split_axis = true;
            split_point = (box.getMaxX() + box.getMinX()) / 2.0;
        }
        left = new RectTreeNode(this, true);
        right = new RectTreeNode(this, false);
        boolean need_left = left.upper_bound > bsf_len && left.len_mid_point < left.upper_bound;
        boolean need_right = right.upper_bound > bsf_len && right.len_mid_point < right.upper_bound;
        if(need_left && need_right){
            if(left.upper_bound > right.upper_bound){
                left.split();
                right.split();
            }else{
                right.split();
                left.split();
            }
        }
        else if(need_left)   left.split();
        else if(need_right) right.split();
    }

    private void insertSeg(int tick){
        if(parent!=null && parent.isSurround.get(cur_valid_segs.size()-1)){
            isSurround.add(true);
            isIntersection.add(true);
            isHit.add(true);
        }else if(parent!=null && !parent.isIntersection.get(cur_valid_segs.size()-1)){
            isSurround.add(false);
            isIntersection.add(false);
            isHit.add(false);
        }else{
            Polygon2D segment_poly = cur_valid_segs.get(cur_valid_segs.size() - 1).getPolygonKB();
            Polygon2D intersect = Polygon2DUtils.intersection(box.asRectangle(), segment_poly);
            if(intersect.getVertexNumber() <=1){
                isIntersection.add(false);
                isSurround.add(false);
                isHit.add(false);
            }else{
                isIntersection.add(true);
                Collection<Point2D> vertices = box.vertices();
                boolean res = true;
                for(Point2D vertex:vertices){
                    if(!segment_poly.contains(vertex)){
                        res = false;
                        break;
                    }
                }
                isSurround.add(res);
                if(res) isHit.add(true);
                else{
                    isHit.add(segment_poly.contains((box.getMinX()+box.getMaxX())/2.0, (box.getMinY() + box.getMaxY())/2.0));
                }
            }
        }

        if(isIntersection.get(isIntersection.size()-1)){
            int prev_end = -1;
            for(int i=isIntersection.size()-2;i>=0;--i){
                if(isIntersection.get(i)){
                    prev_end = cur_valid_segs.get(i).getEnd();
                    break;
                }
            }
            if(prev_end == -1 || prev_end < tick - min_seg_length + 1){
                upper_bound += min_seg_length;
            }else{
                upper_bound += (tick - prev_end);
            }
        }
        if(isHit.get(isHit.size()-1)){
            int prev_end = -1;
            for(int i=isHit.size()-2;i>=0;--i){
                if(isHit.get(i)){
                    prev_end = cur_valid_segs.get(i).getEnd();
                    break;
                }
            }
            if(prev_end == -1 || prev_end < tick - min_seg_length + 1){
                len_mid_point += min_seg_length;
            }else{
                len_mid_point += (tick - prev_end);
            }
        }

    }

    private void deleteSeg(int tick){
//        assert isIntersection.size() == cur_valid_segs.size() - 1;
//        assert isHit.size() == cur_valid_segs.size() - 1;
//        assert isSurround.size() == cur_valid_segs.size() - 1;
        boolean isI = isIntersection.get(0);
        isSurround.remove(0);
        boolean isH = isHit.get(0);
        isIntersection.remove(0);
        isHit.remove(0);
        if(isI){
            int next_start = -1;
            for(int i=0;i<isIntersection.size();++i){
                if(isIntersection.get(i)){
                    next_start = cur_valid_segs.get(i).getStart();
                    break;
                }
            }
            int first_end = tick - window_length + min_seg_length - 1;
            if(next_start == -1 || next_start > first_end){
                upper_bound -= min_seg_length;
            }else{
                upper_bound -= (next_start - (tick-window_length));
            }
        }
        if(isH){
            int next_start = -1;
            for(int i=0;i<isHit.size();++i){
                if(isHit.get(i)){
                    next_start = cur_valid_segs.get(i).getStart();
                    break;
                }
            }
            int first_end = tick - window_length + min_seg_length - 1;
            if(next_start == -1 || next_start > first_end){
                len_mid_point -= min_seg_length;
            }else{
                len_mid_point -= (next_start - (tick-window_length));
            }
        }

    }

    private boolean update_bsf(){
        if (bsf_len > upper_bound) {
            remove_subtrees();
            return false;
        } else if (bsf_len == upper_bound) { // 不能remove_subtrees，因为此时这颗节点矩形的子树上的UB和len根本都没有更新。如果不走updateThirdStage函数，即子节点没有走insertSeg(是有可能重新计算VI/VS/VH的，即UB可能变大)/deleteSeg(ub变小)，
            // remove_subtrees();
            // //有可能当前bsf_node就在这颗树的某个子孙节点上。然后这个目标节点的ub和len由于路径被删除一直没被更新（可能变大）
            return true;
        } else {
            boolean tag = true;
            if (bsf_len < len_mid_point) {

                bsf_node = this;
                bsf_len = len_mid_point;
            }
            if (upper_bound > len_mid_point) {
                if (left == null && right == null) {
                    split();
                    tag = false;
                }
            } else {
                remove_subtrees();
                tag = false;
            }
            return tag;
        }
    }

    private RectTreeNode expand_root(boolean[] expansion, double[] new_box){
        RectTreeNode old_root = this;
        if(expansion[0]){
            RectTreeNode new_root = new RectTreeNode();
            new_root.box = new Box2D(new_box[0], root.box.getMaxX(), root.box.getMinY(), root.box.getMaxY());
            Point2D mid_point = new Point2D((new_box[0] + root.box.getMaxX()) / 2.0,(root.box.getMinY() + root.box.getMaxY()) / 2.0);
            ArrayList<PLASegment>lb_segs = new ArrayList<>();
            for(PLASegment segment:cur_valid_segs){
                new_root.isIntersection.add(true);
                new_root.isSurround.add(false);
                if(segment.getPolygonKB().contains(mid_point)) {
                    new_root.isHit.add(true);
                    lb_segs.add(segment);
                }else   new_root.isHit.add(false);
            }
            // upper and lower bound
            new_root.upper_bound = PLARegionSearch.calcUpperBound(cur_valid_segs);
            new_root.len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
            // update bsf
            if(new_root.len_mid_point > bsf_len){
                bsf_len = new_root.len_mid_point;
                bsf_node = new_root;
            }
            // split line
            new_root.split_axis = true;
            new_root.split_point = root.box.getMinX();
            // left child
            new_root.left = new RectTreeNode(new_root, true);
            new_root.right = root;

            root = new_root;
        }
        if(expansion[1]){
            RectTreeNode new_root = new RectTreeNode();
            new_root.box = new Box2D(root.box.getMinX(), new_box[1], root.box.getMinY(), root.box.getMaxY());
            Point2D mid_point = new Point2D((root.box.getMinX()+ new_box[1]) / 2.0,(root.box.getMinY() + root.box.getMaxY()) / 2.0);

            ArrayList<PLASegment>lb_segs = new ArrayList<>();
            for(PLASegment segment:cur_valid_segs){
                new_root.isIntersection.add(true);
                new_root.isSurround.add(false);
                if(segment.getPolygonKB().contains(mid_point)) {
                    new_root.isHit.add(true);
                    lb_segs.add(segment);
                }else   new_root.isHit.add(false);
            }
            // upper and lower bound
            new_root.upper_bound = PLARegionSearch.calcUpperBound(cur_valid_segs);
            new_root.len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
            // update bsf
            if(new_root.len_mid_point > bsf_len){
                bsf_len = new_root.len_mid_point;
                bsf_node = new_root;
            }
            // split line
            new_root.split_axis = true;
            new_root.split_point = root.box.getMaxX();
            // left child
            new_root.right = new RectTreeNode(new_root, false);
            new_root.left = root;

            root = new_root;
        }
        if(expansion[2]){
            RectTreeNode new_root = new RectTreeNode();
            new_root.box = new Box2D(root.box.getMinX(), root.box.getMaxX(), new_box[2], root.box.getMaxY());
            Point2D mid_point = new Point2D((root.box.getMinX()+ root.box.getMaxX()) / 2.0,(new_box[2]+ root.box.getMaxY()) / 2.0);

            ArrayList<PLASegment>lb_segs = new ArrayList<>();
            for(PLASegment segment:cur_valid_segs){
                new_root.isIntersection.add(true);
                new_root.isSurround.add(false);
                if(segment.getPolygonKB().contains(mid_point)) {
                    new_root.isHit.add(true);
                    lb_segs.add(segment);
                }else   new_root.isHit.add(false);
            }
            // upper and lower bound
            new_root.upper_bound = PLARegionSearch.calcUpperBound(cur_valid_segs);
            new_root.len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
            // update bsf
            if(new_root.len_mid_point > bsf_len){
                bsf_len = new_root.len_mid_point;
                bsf_node = new_root;
            }
            // split line
            new_root.split_axis = false;
            new_root.split_point = root.box.getMinY();
            // left child
            new_root.left = new RectTreeNode(new_root, true);
            new_root.right = root;

            root = new_root;
        }
        if(expansion[3]){
            RectTreeNode new_root = new RectTreeNode();
            new_root.box = new Box2D(root.box.getMinX(), root.box.getMaxX(), root.box.getMinY(), new_box[3]);
            Point2D mid_point = new Point2D((root.box.getMinX() + root.box.getMaxX()) / 2.0,(root.box.getMinY() + new_box[3]) / 2.0);

            ArrayList<PLASegment>lb_segs = new ArrayList<>();
            for(PLASegment segment:cur_valid_segs){
                new_root.isIntersection.add(true);
                new_root.isSurround.add(false);
                if(segment.getPolygonKB().contains(mid_point)) {
                    new_root.isHit.add(true);
                    lb_segs.add(segment);
                }else   new_root.isHit.add(false);
            }
            // upper and lower bound
            new_root.upper_bound = PLARegionSearch.calcUpperBound(cur_valid_segs);
            new_root.len_mid_point = PLARegionSearch.calcUpperBound(lb_segs);
            // update bsf
            if(new_root.len_mid_point > bsf_len){
                bsf_len = new_root.len_mid_point;
                bsf_node = new_root;
            }
            // split line
            new_root.split_axis = false;
            new_root.split_point = root.box.getMaxY();
            // left child
            new_root.right = new RectTreeNode(new_root, false);
            new_root.left = root;

            root = new_root;
        }
        return old_root;
    }

    private void update(Point2D[] point2Ds, int tick, TSPLAPointBoundKBMiner miner){
        int start = cur_valid_segs.get(0).getStart();
        boolean is_delete = false, is_insert = false;
        if(start == tick - window_length){
            is_delete = true;
//            bsf_len -= min_seg_length;
            int bsf_start = -1;
            for(int i=0;i<cur_valid_segs.size();++i){
                if(bsf_node.isHit.get(i)){
                    bsf_start = cur_valid_segs.get(i).getStart();
                    break;
                }
            }
            if(bsf_start == tick - window_length){
               int bsf_second_start = -1;
               boolean tag = false;
               for(int i=0;i<cur_valid_segs.size();++i){
                    if(bsf_node.isHit.get(i) && tag){
                        bsf_second_start = cur_valid_segs.get(i).getStart();
                        break;
                    }else if(bsf_node.isHit.get(i) && !tag){
                        tag = true;
                    }
                }
               if(bsf_second_start == -1 || bsf_second_start > bsf_start + min_seg_length - 1)
                   bsf_len -= min_seg_length;
               else
                   bsf_len -= (bsf_second_start - bsf_start);
            }
            cur_valid_segs.remove(0);
        }
        PLASegment new_seg = miner.buildSpecificSingleSegment(point2Ds, min_seg_length, tick - min_seg_length + 1);
        RectTreeNode update_root = root;
        if(new_seg!= null){
            cur_valid_segs.add(new_seg);
            Collection<Point2D> vertices = new_seg.getPolygonKB().getVertices();
            double min_x=Double.MAX_VALUE,max_x=-min_x,min_y=Double.MAX_VALUE,max_y= -min_y;
            for(Point2D vertex:vertices){
                min_x = Math.min(min_x, vertex.getX());
                max_x = Math.max(max_x, vertex.getX());
                min_y = Math.min(min_y, vertex.getY());
                max_y = Math.max(max_y, vertex.getY());
            }
            boolean[] expansion = {false,false,false,false};
            double[] new_box = {0,0,0,0};
            if(min_x < box.getMinX()) {
                expansion[0] = true;
                new_box[0] = min_x;
            }
            if(max_x > box.getMaxX()) {
                expansion[1] = true;
                new_box[1] = max_x;
            }
            if(min_y < box.getMinY()) {
                expansion[2] = true;
                new_box[2] = min_y;
            }
            if(max_y > box.getMaxY()) {
                expansion[3] = true;
                new_box[3] = max_y;
            }
            is_insert = true;
            if(expansion[0] || expansion[1] || expansion[2] || expansion[3])
                update_root = expand_root(expansion, new_box);
        }

        if(is_delete || is_insert)
            updateThirdStage(update_root, is_delete, is_insert, tick);

    }

    private void updateThirdStage(RectTreeNode update_root, boolean is_delete, boolean is_insert, int tick){
        if(update_root == null) return;
        boolean tag = true;
        if(is_insert && is_delete){
            update_root.deleteSeg(tick);
            update_root.insertSeg(tick);
            tag = update_root.update_bsf();
        }else if(is_insert){
            update_root.insertSeg(tick);
            tag = update_root.update_bsf();
        }else if(is_delete){
            update_root.deleteSeg(tick);
            tag = update_root.update_bsf();
        }

        if(!tag)    return;

        if(update_root.upper_bound == 0)    update_root.remove_subtrees();
        else{
            updateThirdStage(update_root.left, is_delete, is_insert, tick);
            updateThirdStage(update_root.right, is_delete, is_insert, tick);
        }

    }

    private void remove_subtrees() {
        left = null;
        right = null;
    }

    public static void main(String[] args) throws IOException {
        int length = 74000;
        String fileName = "data/experiments/5_USD_CAD_MXN_1.txt";
        Point2D[] point2Ds = ClimateDATAUtils.readPointsFromFile(new File(fileName), length);

        Point2D[] init_points = new Point2D[window_length];
        System.arraycopy(point2Ds, 0, init_points, 0, window_length);
        TSPLAPointBoundKBMiner miner = new TSPLAPointBoundKBMiner(init_points, error_bound);
        cur_valid_segs = miner.buildSpecificSegments(min_seg_length);

        root = new RectTreeNode();
        root.initialize();
        System.out.println(bsf_len);

        // runtime vs length 效率实验
        List<Long> graphPoints = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        long totalTime = 0;
        int totalCount = 0;
        for (int i = window_length; i < length; ++i) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            root.update(point2Ds, i, miner); // 更新矩形树
            stopWatch.stop();

            long iTime = stopWatch.getTime(); // 单位是毫秒
            totalTime += iTime;

            int icount = root.countNodes();
            totalCount += icount;
            // System.out.println("i: " + i + "," + "iTime : " + iTime);
            System.out.println("i: " + i + "," + "iTime : " + iTime + ", totalTime: " + totalTime + ", icount: "
                    + icount + ", bsflen: " + bsf_len);

            if ((i + 1) % 1000 == 0) {
                int pointIndex = ((i + 1 - window_length) / 1000); // (1,1000) （2,1000)....(74,1000)
                long point1000Time = totalTime / 1000;
                int averageCount = totalCount / 1000;
                System.out.println("pointIndex: " + pointIndex + ", point1000Time : " + point1000Time
                        + ", averageCount : " + averageCount);
                graphPoints.add(point1000Time);
                counts.add(averageCount);
                totalTime = 0;
                totalCount = 0;
            }
        }
        for (int i = 0; i < graphPoints.size(); i++) {
            System.out.println(i + "    :    " + graphPoints.get(i));
        }
        System.out.println("counts    :    ");
        for (int i = 0; i < counts.size(); i++) {
            System.out.println(i + "    :    " + counts.get(i));
        }
    }

    public static Point2D[] readFromFile(String filename, int columnA, int columnB) throws IOException {
        File file = new File(filename);
        List list = FileUtils.readLines(file);
        Point2D[] data;
        data = new Point2D[list.size()];
        int cnt2 = 0;
        for (int i = 0; i < list.size(); i++) {
            String s = (String) list.get(i);
            String[] strings = s.split(",");
            data[cnt2] = new Point2D(Double.parseDouble(strings[columnA]), Double.parseDouble(strings[columnB]));
            cnt2++;
        }
        return data;
    }

    public int countNodes() {
        // 用递归的方式,用递归真的太简单了
        // if(root == NULL)
        // return 0;
        // return
        // 1+countNodes(root->left)+countNodes(root->right);//属于后序遍历，先求了子树的，然后加上了中间的根节点
        // 还可以采用层序遍历的方式，稍微改动模板，其实前序后序中序遍历感觉都行
        if (root == null)
            return 0;
        Queue<RectTreeNode> que = new LinkedList<>();
        que.add(root);
        int result = 0;
        while (!que.isEmpty()) {
            int size = que.size();
            for (int i = 0; i < size; i++) {
                RectTreeNode node = que.peek();
                que.poll();
                if (node.left != null)
                    que.add(node.left);
                if (node.right != null)
                    que.add(node.right);
                result++;
            }
        }
        return result;
    }
}
