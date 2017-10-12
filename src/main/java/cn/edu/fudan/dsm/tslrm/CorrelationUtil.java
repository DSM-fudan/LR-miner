package cn.edu.fudan.dsm.tslrm;

import org.apache.commons.collections.CollectionUtils;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-30
 * Time: 上午10:22
 * To change this template use File | Settings | File Templates.
 */
public class CorrelationUtil {
    public static double calcCorrelation(Set set1, Set set2)
    {
        int unionSize = CollectionUtils.union(set1, set2).size();
        int intersectionSize = CollectionUtils.intersection(set1, set2).size();
        return intersectionSize*1.0/unionSize;
    }
}
