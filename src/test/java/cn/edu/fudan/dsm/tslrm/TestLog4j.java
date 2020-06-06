package cn.edu.fudan.dsm.tslrm;

import cn.edu.fudan.dsm.tslrm.log.DefaultFileAppender;
import junit.framework.Assert;
import org.apache.log4j.*;
import org.junit.Test;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: wangyang
 * Date: 12-10-30
 * Time: 下午7:50
 * To change this template use File | Settings | File Templates.
 */
public class TestLog4j {
    static Logger logger = Logger.getLogger(TestLog4j.class);

    @Test
    public void test() throws IOException {
//        FileAppender newAppender = new FileAppender(new PatternLayout("%d %p [%c %L] - <%m>%n"),this.getClass().getSimpleName()+".log");

//        Logger.getRootLogger().addAppender(newAppender);

        Logger.getRootLogger().addAppender(new DefaultFileAppender(this.getClass()));
        logger.debug("debug");
        logger.error("error");
    }

    @Test
    public void testAdd()
    {
        int i = 1;
        int j = 3;
        Assert.assertEquals(4.2,1+3);
    }


}
