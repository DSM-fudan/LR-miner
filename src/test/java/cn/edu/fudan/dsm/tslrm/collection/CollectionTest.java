package cn.edu.fudan.dsm.tslrm.collection;

import cn.edu.fudan.dsm.tslrm.CorrelationUtil;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 13-6-30
 * Time: 上午10:09
 * To change this template use File | Settings | File Templates.
 */
public class CollectionTest {

    @Test
    public void testHashSet()
    {
        Set set1 = new HashSet();
        set1.add(1);
        set1.add(2);
        set1.add(3);

        Set set2 = new HashSet();
        set2.add(4);
        set2.add(2);
        set2.add(3);

        boolean b = set1.containsAll(set2);
        System.out.println("b = " + b);

    }

    @Test
    public void testCollectionOperation()
    {
        Set set1 = new HashSet();
        set1.add(1);
        set1.add(2);
        set1.add(3);

        Set set2 = new HashSet();
        set2.add(4);
        set2.add(2);
        set2.add(3);

        Collection union = CollectionUtils.union(set1, set2);
        System.out.println("union.size() = " + union.size());

        Collection intersection = CollectionUtils.intersection(set1, set2);
        System.out.println("intersection.size() = " + intersection.size());

        double v = CorrelationUtil.calcCorrelation(set1, set2);
        System.out.println("v = " + v);
    }
}
